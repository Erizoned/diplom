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

prompt = "Пользователь сказал: " + serverPrompt + ". Вырежи из его запроса ключевые слова, которые обозначают его предпочтения(ingredients(это объект, который содержит внутри себя name и count), всё что идёт дальше находится внутри объекта recipe и является одной строкой: kkal(калорийность), count_portion, restrictions, national_kitchen, theme, time_to_cook, type_of_cook(сковорода, духовка и тд), type_of_food(Гарнир, Завтрак и тд)). Ответь в формате JSON где будут записаны все данные что ты смог найти в тексте. Все поля которые с числами должны быть integer(то есть только числа без слов). Вместо использования '_'  в качестве пробела убери их и делай каждую букву после пробела большой(пример:timeToCook)"
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