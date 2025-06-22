import base64
import sys
import io
import os
import re
import requests
from google import genai
from google.genai import types
import json
import textwrap
from deep_translator import GoogleTranslator
from pydantic import BaseModel
from typing import List

class Ingredient(BaseModel):
    name: str
    count: int
    unit: str

class Step(BaseModel):
    stepNumber: int
    description: str

class RecipeMeta(BaseModel):
    name: str
    description: str
    kkal: int
    countPortion: int
    restrictions: str
    nationalKitchen: str
    theme: str
    timeToCook: int
    typeOfCook: str
    typeOfFood: str

class FullRecipe(BaseModel):
    recipe: RecipeMeta
    ingredients: List[Ingredient]
    steps: List[Step]

# Настройка переводчика и клиента GenAI
translator = GoogleTranslator(source='auto', target='en')
client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))

# Конфигурация окружения
UNSPLASH_ACCESS_KEY = os.getenv("UNSPLASH_ACCESS_KEY")
if not UNSPLASH_ACCESS_KEY:
    raise RuntimeError("Env var UNSPLASH_ACCESS_KEY is not set")

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

JWT_TOKEN = os.getenv("JWT_TOKEN")
if not JWT_TOKEN:
    raise RuntimeError("Env var JWT_TOKEN is not set")

urlForCreation = "http://localhost:8081/api/create_recipe"

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def gen_recipe(prompt_text: str) -> dict:
    server_prompt = (
        f"Пользователь сказал: {prompt_text}. "
        "Создай JSON рецепта по этому названию. Ты должен указать: "
        "(ingredients — список объектов {name, count, unit}, unit - это (гр., мл., шт.)), "
        "steps — список объектов {stepNumber, description}, "
        "и внутри recipe: name, description, kkal, count_portion, restrictions, "
        "national_kitchen, theme, time_to_cook, type_of_cook, type_of_food. "
        "Все числовые поля — integer. Поля с пробелами оформляй в camelCase. Все поля типа type_of_cook, type_of_food,  national_kitchen и так далее должны быть на русском"
        "theme - это обозначение темы блюда (завтрак, обед, ужин) и только"
        "Strictly respond with raw JSON only."
        "Ни в коем случае не создавай не существующие рецепты. Создавай только то, что может быть приготовлено в реальной жизни и не противеречит логике(например, свинные крылышки). Если пользователь попросил создать рецепт чего-то запрещённого, опасного или несуществующего, то просто возвращай null"
    )

    response = client.models.generate_content(
        model="gemini-1.5-flash",
        contents=server_prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": FullRecipe,  
        }
    )

    return response.parsed.model_dump()

def sanitize_filename(name: str) -> str:
    return re.sub(r'[<>:"/\\|?*]', '', name.replace(' ', '_'))[:100]

# Генерация изображения через Gemini с использованием generate_images
def generate_image(prompt: str, folder="images") -> str | None:
        try:
            result = client.models.generate_content(
                model="gemini-2.0-flash-exp-image-generation",
                contents=prompt,
                config=types.GenerateContentConfig(
                    response_modalities=['TEXT', 'IMAGE']
                )
            )
            inline = next((p.inline_data for p in result.candidates[0].content.parts if p.inline_data), None)
            if not inline:
                print("Gemini не вернул изображение")
                return None
            raw = inline.data
            image_bytes = base64.b64decode(raw) if isinstance(raw, str) else raw
            safe_name = sanitize_filename(prompt) + ".png"
            return safe_name, image_bytes
        except Exception as e:
            print(f"[ERROR] Image generation failed: {e}")
            return None

# Обогащение рецепта изображениями
def enrich_with_images(recipe: dict) -> dict:
    title_eng = translator.translate(recipe["recipe"]["name"])
    for step in recipe.get("steps", []):
        eng = translator.translate(step["description"])
        newPrompt = f"A clean, realistic illustration of one step of cooking {title_eng}: {eng}. No text, no labels. Picture must be connected with {title_eng} "
        print("Запрос для изображения:", newPrompt)
        img = generate_image(newPrompt)
        if img:
            name, data = img
            step["image_name"] = name
            step["image_data"] = data
    newTitlePrompt = f"A realistic image of the dish: {title_eng}. Do not include any text or labels. Picture must be connected with {title_eng}"
    cover = generate_image(newTitlePrompt)
    if cover:
        cn, cd = cover
        recipe["cover_image_name"] = cn
        recipe["cover_image_data"] = cd
    return recipe

# Отправка рецепта на сервер
def post_recipe(recipe: dict):
    files = []
    if recipe.get("cover_image_data"):
        files.append((
            "photoFood",
            (recipe["cover_image_name"],
             io.BytesIO(recipe["cover_image_data"]),
             "image/png")
        ))
        
    for st in recipe.get("steps", []):
        if st.get("image_data"):
            files.append((
                "stepPhotos",
                (st["image_name"],
                 io.BytesIO(st["image_data"]),
                 "image/png")
            ))
    data = {
        "name": recipe["recipe"]["name"],
        "description": recipe["recipe"]["description"],
        "kkal": str(recipe["recipe"]["kkal"]),
        "countPortion": str(recipe["recipe"]["countPortion"]),
        "restrictions": recipe["recipe"].get("restrictions", ""),
        "nationalKitchen": recipe["recipe"].get("nationalKitchen", ""),
        "theme": recipe["recipe"].get("theme", ""),
        "timeToCook": str(recipe["recipe"]["timeToCook"]),
        "typeOfCook": recipe["recipe"].get("typeOfCook", ""),
        "typeOfFood": recipe["recipe"].get("typeOfFood", ""),
        "stepDescriptions": [st["description"] for st in recipe.get("steps", [])],
        "ingredientNames": [ing["name"] for ing in recipe.get("ingredients", [])],
        "ingredientsCounts": [str(ing["count"]) for ing in recipe.get("ingredients", [])],
        "ingredientUnits": [str(ing["unit"]) for ing in recipe.get("ingredients", [])],
    }
    headers = {"Authorization": f"Bearer {os.getenv('JWT_TOKEN')}"}
    print(">>> files:", [f[0] for f in files])
    print(">>> data keys:", data.keys())
    resp = requests.post(urlForCreation, files=files, data=data, headers=headers)
    try:
        print("<<< Response status:", resp.status_code)
        print("<<< Response content-type:", resp.headers.get("Content-Type"))
        print("<<< RAW body:", resp.text)
        response_data = resp.json()
        recipe_id = response_data.get("id")
        print(f" Recipe ID: {recipe_id}")
    except requests.exceptions.JSONDecodeError:
        print(" Ошибка: Сервер вернул не-JSON. ")
        print("Ответ:", resp.text)

# Основной запуск
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py \"Название рецепта\"")
        sys.exit(1)
    recipe = gen_recipe(sys.argv[1])
    recipe = enrich_with_images(recipe)
    post_recipe(recipe)
