import os
from diffusers import DiffusionPipeline
from diffusers import FluxPipeline
import torch
from io import BytesIO
import google.generativeai as genai
import base64
import requests

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    raise RuntimeError("Env var GOOGLE_API_KEY is not set")

genai.configure(api_key=GOOGLE_API_KEY)
model = genai.GenerativeModel('gemini-2.0-flash-exp-image-generation')

# model_path = "C:\\Users\\Anton\\Documents\\GitHub\\diplom\\receipt\\models\\sdxl-base"


# pipe = FluxPipeline.from_pretrained(
#     model_path,
#     torch_dtype=torch.float16,
#     use_safetensors=True,
# )

# pipe.enable_model_cpu_offload()

prompt = "Prepare the vegetables: peel and dice the potatoes, onions and carrots. Cut the tomato into circles."

response = model.generate_content(prompt)

base64_image = response.candidates[0].content.parts[0].inline_data.data

# pipe.unet = torch.compile(pipe.unet, mode="reduce-overhead", fullgraph=True)

# images = pipe(prompt=prompt).images[0]

# buf = BytesIO()
# images.save(buf, format="PNG")
# buf.seek(0)

# files = {
#   "image": ("astronaut.png", buf, "image/png")
# }
files = {
    'image': ('image.png', BytesIO(base64.b64decode(base64_image)), 'image/png')
}
requests.post("http://localhost:8081/api/script/show_image", files=files)