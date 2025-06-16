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

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

urlForCreation = "http://localhost:8081/api/create/recipe/default/{id}"

def gen_recipe(prompt_text: str) -> dict:
    server_prompt = (
        f"У пользователя есть следующие ингредиенты: {prompt_text}. Напиши название трёх блюд через запятую, которые можно приготовить с этими ингредиентами. Пиши только названия блюд и больше ничего"
    )

    response = client.models.generate_content(
        model="gemini-1.5-flash",
        contents=server_prompt
    )
    print(response.text.strip())
    return response.text.strip()

if __name__ == "__main__":
    prompt = sys.argv[1]
    recipe = gen_recipe(prompt)
    
