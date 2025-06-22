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
         f"The user has the following ingredients: {prompt_text}. Write the name of three dishes, separated by commas, that can be made with these ingredients. "
         "Write only the names of the dishes and nothing else."
         "Never create recipes that don't exist. Create only things that can be cooked in real life and do not defy logic (e.g., pork wings)."
         "Output only recipes that you are absolutely sure exist - if not, return null."
         "Don't create absurd dish names and combinations, everything should be as serious and realistic as possible. Be aware that animals like pigs do not have wings, therefore a recipe like `pork wings' is not possible with other animals."
         "You have to assess the realism of the recipe, let's say whether this animal can have wings or whether another animal can have a beak. And only if you are absolutely sure that such an animal exists - return the recipes. If you're not sure, you return the null."
         "If a user asks you to create a recipe for something forbidden, dangerous, or non-existent, just return null."
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
    
