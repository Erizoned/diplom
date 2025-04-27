import sys
import io
import os
import requests
import google.generativeai as genai
import json
import textwrap
from googletrans import Translator as GTranslator

translator = GTranslator() 

UNSPLASH_ACCESS_KEY = os.getenv("UNSPLASH_ACCESS_KEY")
if not UNSPLASH_ACCESS_KEY:
    raise RuntimeError("Env var UNSPLASH_ACCESS_KEY is not set")

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

JWT_TOKEN = os.getenv("JWT_TOKEN")
if not JWT_TOKEN:
    raise RuntimeError("Env var JWT_TOKEN is not set")

UNSPLASH_ENDPOINT = "https://api.unsplash.com/search/photos"


sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def to_markdown(text):
    text = text.replace('•', '  *')
    return textwrap.indent(text, '> ', predicate=lambda _: True)

urlForCreation = "http://localhost:8081/api/create_recipe"

genai.configure(api_key=GOOGLE_API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash')

def gen_recipe(prompt_text: str) -> dict:
    server_prompt = (
        "Пользователь сказал: " + prompt_text +
        ". Создай JSON рецепта по этому названию. Ты должен указать: "
        "(ingredients — список объектов {name, count, unit}), "
        "steps — список объектов {stepNumber, description}, "
        "и внутри recipe: name, description, kkal, count_portion, restrictions, "
        "national_kitchen, theme, time_to_cook, type_of_cook, type_of_food. "
        "Все числовые поля — integer. Поля с пробелами оформляй в camelCase. "
        "Strictly respond with raw JSON only, no markdown code fences or backticks."
    )
    resp = model.generate_content(server_prompt)
    try:
        print("JSON: ", resp.text)
        return json.loads(resp.text)
    except json.JSONDecodeError as e:
        print("Failed to parse JSON:", e)
        print("Response was:", resp.text)
        sys.exit(1)

def unsplash_search_image(query: str, per_page=1) -> str|None:
    headers = {"Authorization": f"Client-ID {UNSPLASH_ACCESS_KEY}"}
    params  = {"query": query, "per_page": per_page, "orientation": "landscape"}
    r = requests.get(UNSPLASH_ENDPOINT, headers=headers, params=params); r.raise_for_status()
    items = r.json().get("results", [])
    return items[0]["urls"]["regular"] if items else None

def download_image(url: str, folder="images") -> str:
    os.makedirs(folder, exist_ok=True)
    r = requests.get(url, stream=True); r.raise_for_status()
    fn = url.split("/")[-1].split("?")[0] + ".jpg"
    path = os.path.join(folder, fn)
    with open(path, "wb") as f:
        for chunk in r.iter_content(1024):
            f.write(chunk)
    return path

def enrich_with_images(recipe: dict) -> dict:
    # для каждого шага
    for step in recipe["steps"]:
        desc = step["description"]
        engDesc = translator.translate(desc, dest="en").text  
        print("Запрос для изображения:", engDesc)
        url  = unsplash_search_image(engDesc)
        step["image_path"] = download_image(url) if url else None

    # обложка
    title = recipe["recipe"]["name"]
    engTitle = translator.translate(title, dest="en").text
    print("Запрос для изображения:", engTitle)
    cover_url = unsplash_search_image(engTitle)
    recipe["cover_image_path"] = download_image(cover_url) if cover_url else None

    return recipe

# ——————————————————————————————————————————————————————————
# 4) Формируем multipart и отправляем
# ——————————————————————————————————————————————————————————
def post_recipe(recipe: dict):
    # Собираем файлы
    files = []

    if recipe.get("cover_image_path"):
        files.append((
            "photoFood",
            (os.path.basename(recipe["cover_image_path"]),
             open(recipe["cover_image_path"], "rb"),
             "image/jpeg")
        ))

    for st in recipe["steps"]:
        if st.get("image_path"):
            files.append((
                "stepPhotos",
                (os.path.basename(st["image_path"]),
                 open(st["image_path"], "rb"),
                 "image/jpeg")
            ))

    # Флэт-данные для Recipe и массивов
    data = {
        # Свойства Recipe
        "name":            recipe["recipe"]["name"],
        "description":     recipe["recipe"]["description"],
        "kkal":            str(recipe["recipe"]["kkal"]),
        "countPortion":    str(recipe["recipe"]["countPortion"]),
        "restrictions":    recipe["recipe"].get("restrictions", ""),
        "nationalKitchen": recipe["recipe"].get("nationalKitchen", ""),
        "theme":           recipe["recipe"].get("theme", ""),
        "timeToCook":      str(recipe["recipe"]["timeToCook"]),
        "typeOfCook":      recipe["recipe"].get("typeOfCook", ""),
        "typeOfFood":      recipe["recipe"].get("typeOfFood", ""),

        # Массивы (Spring сам распознает повторяющиеся ключи)
        # requests правильно сериализует список в stepDescriptions=…&stepDescriptions=…
        "stepDescriptions": recipe["steps"] and [st["description"] for st in recipe["steps"]],
        "ingredientNames":   [ing["name"] for ing in recipe["ingredients"]],
        "ingredientsCounts": [str(ing["count"]) for ing in recipe["ingredients"]],
    }

    headers = {
        "Authorization": f"Bearer {JWT_TOKEN}"
    }

    resp = requests.post(urlForCreation, files=files, data=data, headers=headers)
    print("status:", resp.status_code)
    print("body:", resp.text)

# ——————————————————————————————————————————————————————————
# 5) Основной запуск
# ——————————————————————————————————————————————————————————
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py \"Название рецепта\"")
        sys.exit(1)

    user_prompt = sys.argv[1]
    recipe = gen_recipe(user_prompt)
    recipe = enrich_with_images(recipe)
    post_recipe(recipe)