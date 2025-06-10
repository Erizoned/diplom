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

client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

JWT_TOKEN = os.getenv("JWT_TOKEN")
if not JWT_TOKEN:
    raise RuntimeError("Env var JWT_TOKEN is not set")

urlForCreation = "http://localhost:8081/api/create/recipe/default/{id}"

def gen_default_recipe(prompt_text: str) -> dict:
    server_prompt = (
        f"Есть диета {prompt_text}. Придумай для неё ОДИН рецепт. В ответе должно быть только название ОДНОГО рецепта."
    )

    response = client.models.generate_content(
        model="gemini-1.5-flash",
        contents=server_prompt
    )

    return response.text.strip()

def post_recipe(recipe_name: str, diet_id: str):
    url = f"http://localhost:8081/api/script/diet/default/{diet_id}"
    json_data = {"recipeName": recipe_name}
    headers = {
        "Authorization": f"Bearer {JWT_TOKEN}",
        "Content-Type": "application/json"
    }
    resp = requests.post(url, json=json_data, headers=headers)
if __name__ == "__main__":
    prompt = sys.argv[1]
    diet_id = sys.argv[2]
    recipe = gen_default_recipe(prompt)
    post_recipe(recipe, diet_id)
