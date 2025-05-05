import base64
from PIL import Image
import sys
import io
import os
import requests
from google import genai
from google.genai import types
import json
import textwrap
from deep_translator import GoogleTranslator

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

# Вспомогательная функция для блоков Markdown
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
def to_markdown(text):
    text = text.replace('•', '  *')
    return textwrap.indent(text, '> ', predicate=lambda _: True)

# Генерация JSON-рецепта
def gen_recipe(prompt_text: str) -> dict:
    server_prompt = (
        f"Пользователь сказал: {prompt_text}. "
        "Создай JSON рецепта по этому названию. Ты должен указать: "
        "(ingredients — список объектов {name, count, unit}), "
        "steps — список объектов {stepNumber, description}), "
        "и внутри recipe: name, description, kkal, count_portion, restrictions, "
        "national_kitchen, theme, time_to_cook, type_of_cook, type_of_food. "
        "Все числовые поля — integer. Поля с пробелами оформляй в camelCase. Все поля типа type_of_cook, type_of_food,  national_kitchen и так далее должны быть на русском"
        "Strictly respond with raw JSON only, no markdown code fences or backticks."
    )
    response = client.models.generate_content(
        model="gemini-1.5-flash",
        contents=server_prompt
    )
    try:
        return json.loads(response.text)
    except json.JSONDecodeError as e:
        print("Failed to parse JSON:", e)
        print("Response was:", response.text)
        sys.exit(1)

# Генерация изображения через Gemini с использованием generate_images
def generate_image(prompt: str, folder="images") -> str | None:
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
        safe_name = prompt.replace(" ", "_")[:50] + ".png"
        return safe_name, image_bytes

# Обогащение рецепта изображениями
def enrich_with_images(recipe: dict) -> dict:
    for step in recipe.get("steps", []):
        eng = translator.translate(step["description"])
        newPrompt = "Hi, create an image, where a user can see this step:" + eng + "send only 1 image without any text. Image need to be without any text on it"
        print("Запрос для изображения:", newPrompt)
        img = generate_image(newPrompt)
        if img:
            name, data = img
            step["image_name"] = name
            step["image_data"] = data
    title_eng = translator.translate(recipe["recipe"]["name"])
    newTitlePrompt = "Hi, create an image, where a user can see this recipe:" + title_eng + "send only 1 image without any text"
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
    }
    headers = {"Authorization": f"Bearer {os.getenv('JWT_TOKEN')}"}
    print(">>> files:", [f[0] for f in files])
    print(">>> data keys:", data.keys())
    print(">>> headers:", headers)
    resp = requests.post(urlForCreation, files=files, data=data, headers=headers)
    print(">> Request headers:", resp.request.headers)
    print("status:", resp.status_code)
    print("body:", resp.text)

# Основной запуск
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py \"Название рецепта\"")
        sys.exit(1)
    recipe = gen_recipe(sys.argv[1])
    recipe = enrich_with_images(recipe)
    post_recipe(recipe)
