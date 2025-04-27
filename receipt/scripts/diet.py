import sys
import textwrap
import json
import re
import requests
import io

import google.generativeai as genai

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def to_markdown(text):
    text = text.replace('•', '  *')
    return textwrap.indent(text, '> ', predicate=lambda _: True)

url = "http://localhost:8081/api/preferences"
GOOGLE_API_KEY = 'AIzaSyD3Vz_KGLvVk74VTdgI33rnkpGDKGUGMWg'

genai.configure(api_key=GOOGLE_API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash')

serverPrompt = sys.argv[1]

prompt = "Пользователь сказал: " + serverPrompt + ".Составь ему диету из блюд. Пиши только название и отправь их списком через запятую. Рядом с названиями блюд ничего не пиши, в списке должны быть только названия рецептов. Названия рецептов должны быть максимально простыми. После списка поставь символ ! И добавь рекомендации чтобы помочь человеку справиться с описанной проблемой. Список рецептов составляй так: первые три рецепта на завтра, вторые три рецепта на обед и третьи три рецепта на ужин. "
response = model.generate_content(prompt)

print(response.text)