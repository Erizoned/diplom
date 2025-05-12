import sys
import textwrap
import json
import re
import requests
import io
import os
from google import genai
from google.genai import types

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
client = genai.Client(api_key=os.getenv("GOOGLE_API_KEY"))

def to_markdown(text):
    text = text.replace('•', '  *')
    return textwrap.indent(text, '> ', predicate=lambda _: True)

url = "http://localhost:8081/api/preferences"

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

serverPrompt = sys.argv[1]

prompt = "Пользователь сказал: " + serverPrompt + ".Составь ему диету из блюд. Пиши только название и отправь их списком через запятую. Рядом с названиями блюд ничего не пиши, в списке должны быть только названия рецептов. Названия рецептов должны быть максимально простыми. После списка поставь символ ! И добавь рекомендации чтобы помочь человеку справиться с описанной проблемой. Список рецептов составляй так: первые три рецепта на завтра, вторые три рецепта на обед и третьи три рецепта на ужин. Рекомендация не должна быть больше 1000 символов"
response = client.models.generate_content(
    model="gemini-1.5-flash",
    contents=prompt
)

print(response.text)