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
import urllib.parse

client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

JWT_TOKEN = os.getenv("JWT_TOKEN")
if not JWT_TOKEN:
    raise RuntimeError("Env var JWT_TOKEN is not set")

urlForCreation = "http://localhost:8081/api/create/recipe/default/{id}"

def gen_default_recipe(diet_name: str,
                       old_recipe: str,
                       recipe_role: str,
                       recipes_list: str) -> str:
    server_prompt = (
        f"У пользователя диета «{diet_name}». "
        f"Пользователю не понравился рецепт «{old_recipe}». "
        f"Придумай для этой диеты ОДИН новый рецепт для приёма пищи «{recipe_role}». "
        f"В этом разделе уже есть: {recipes_list}. "
        "НИ В КОЕМ СЛУЧАЕ не повторяй существующие блюда — выведи только название одного уникального рецепта."
        "Старайся делать не слишком длинные и подробные названия рецептов"
    )
    print(server_prompt)
    response = client.models.generate_content(
        model="gemini-1.5-flash",
        contents=server_prompt
    )

    return response.text.strip()

def post_recipe(recipe_name: str, diet_id: str):
    url = f"http://localhost:8081/api/diet/create/recipe/default/{diet_id}?name={urllib.parse.quote(recipe_name)}"
    headers = {
        "Authorization": f"Bearer {JWT_TOKEN}",
        "Content-Type": "application/json"
    }
    resp = requests.post(url, headers=headers)
if __name__ == "__main__":
    old_recipe   = sys.argv[1] 
    diet_id      = sys.argv[2]
    diet_name    = sys.argv[3]
    recipe_role  = sys.argv[5]
    recipes_list = sys.argv[6]
    recipe = gen_default_recipe(diet_name, old_recipe, recipe_role, recipes_list)
    post_recipe(recipe, diet_id)
