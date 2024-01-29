#!/usr/bin/env python3

import PIL.Image
import PIL.ImageOps
import json
from gimpformats.GimpLayer import GimpLayer
from gimpformats.gimpXcfDocument import GimpDocument

project = GimpDocument("scripts/layers.xcf")
project.forceFullyLoaded()

def layer_children(base_layer: GimpLayer):
    path = base_layer.itemPath
    layers = []
    for layer in project.layers:
        if not layer.itemPath or not layer.visible:
            continue
        if len(layer.itemPath) == len(path) + 1 and layer.itemPath[:len(path)] == path:
            layer.applyMask = True
            if layer.mask is not None:
                tmp_image = PIL.Image.new(mode = "RGBA", size = (layer.width, layer.height))
                layer.mask.decode(layer._data, layer._maskPtr)  # Dunno why we need this hack, but we do.
                mask_image = PIL.ImageOps.invert(layer.mask.image)
                image = PIL.Image.composite(tmp_image, layer.image.convert(mode = "RGBA"), mask_image)
            else:
                image = layer.image

            if layer.opacity != 1.0:
                alpha = image.getchannel('A')
                alpha = alpha.point(lambda x: x * layer.opacity)
                image.putalpha(alpha)

            layers.append((layer.name, image, layer.getBlendMode()))
    return layers

kinds = []
for layer in project.layers:
    if layer.itemPath and len(layer.itemPath) == 2:
        normalized_name = layer.name.replace(" ", "_").replace("#", "").lower()
        kinds.append((normalized_name, layer_children(layer)))
kinds.sort(key = lambda x: x[0])

images = sum(map(lambda x: len(x[1]), kinds))
size = max(map(lambda x: max(map(lambda x: max(x[1].height, x[1].width), x[1])), kinds))
rows = (images + 15) // 16

out_image = PIL.Image.new(mode = "RGBA", size = (16 * size, rows * size))
def push_image(i, img):
    x, y = i % 16, i // 16
    w, h = img.width, img.height
    box = (x * size, y * size, x * size + w, y * size + h)
    out_image.paste(img, box)
    return box

idx = 0
ore_info = {}
for ore in kinds:
    ore_name, layers = ore
    layer_info = []
    for layer in layers:
        image_bound = push_image(idx, layer[1])
        idx += 1

        layer_info.append({
            "name": layer[0],
            "mode": "Layer",
            "bound": image_bound,
            "blend_mode": layer[2],
        })
    layer_info.append({
        "mode": "External",
        "layer": "base_layer"
    })
    ore_info[ore_name] = layer_info

out_path = "src/main/resources/assets/colorfulmagicaloremod/texture_compositor/"
out_name = "overlays.png"
ore_manifest = {
    "out_name": out_name,
    "image_size": (out_image.width, out_image.height),
    "layers": ore_info,
}
out_image.save(f"{out_path}/{out_name}")
open(f"{out_path}/overlays.json", "w").write(json.dumps(ore_manifest, indent = True))
