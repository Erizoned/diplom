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

prompt = "Пользователь сказал: " + serverPrompt + ". Вырежи из его запроса ключевые слова, которые обозначают его предпочтения(ingredients(это объект, который содержит внутри себя name и count), всё что идёт дальше находится внутри объекта recipe и является одной строкой: name(название блюда) kkal(калорийность), countPortion, restrictions, nationalKitchen, theme(завтрак, обед, ужин), timeToCook, typeOfCook(сковорода, духовка и тд), typeOfFood(Гарнир, Завтрак и тд)). Ответь в формате JSON где будут записаны все данные что ты смог найти в тексте. Все поля которые с числами должны быть integer(то есть только числа без слов). Все поля в JSON пиши в camel case. Ни в коем случе не добавляй новые поля в JSON. Используй только те поля, что я тебе указал. В ингридиентах должны быть только ингридиенты, не добавляй туда готовые блюда типа ( пицца )"
response = model.generate_content(prompt)

pattern = r'```json\s*(.*?)\s*```'

raw_text = response.candidates[0].content.parts[0].text

match = re.search(pattern, raw_text, re.DOTALL)
if match:
  json_str = match.group(1).strip()
else:
  json_str = raw_text.strip()


try:
  data = json.loads(json_str)
except json.JSONDecoderError:
  print(json.dumps({"error": "Невалидный JSON от Gemini"}))
  sys.exit(1)

print(json.dumps(data, ensure_ascii=False))