from diffusers import DiffusionPipeline
from diffusers import FluxPipeline
import torch
from io import BytesIO


model_path = "C:\\Users\\Anton\\Documents\\GitHub\\diplom\\receipt\\models\\sdxl-base"


pipe = FluxPipeline.from_pretrained(
    model_path,
    torch_dtype=torch.float16,
    use_safetensors=True,
)

pipe.enable_model_cpu_offload()

prompt = "An astronaut riding a green horse"

pipe.unet = torch.compile(pipe.unet, mode="reduce-overhead", fullgraph=True)

images = pipe(prompt=prompt).images[0]

buf = BytesIO()
images.save(buf, format="PNG")
buf.seek(0)

files = {
  "image": ("astronaut.png", buf, "image/png")
}
requests.post("http://localhost:8081/api/script/show_image", files)