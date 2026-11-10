#!/usr/bin/env python3
"""
Generate Android TV Icons and Banners from source high-res art.
Creates all required mipmap and drawable densities for Android TV.
"""

import os
from PIL import Image, ImageDraw, ImageFilter

def create_rounded_icon(src_img, size, radius=None):
    img = src_img.resize((size, size), Image.Resampling.LANCZOS).convert("RGBA")
    if radius is None:
        radius = int(size * 0.22) # Modern squircle
    
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), (size - 1, size - 1)], radius=radius, fill=255)
    
    output = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    output.paste(img, (0, 0), mask=mask)
    return output

def create_circular_icon(src_img, size):
    img = src_img.resize((size, size), Image.Resampling.LANCZOS).convert("RGBA")
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse([(0, 0), (size - 1, size - 1)], fill=255)
    
    output = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    output.paste(img, (0, 0), mask=mask)
    return output

def process_assets(icon_path, banner_path, res_dir):
    print(f"Processing Icon from: {icon_path}")
    print(f"Processing Banner from: {banner_path}")
    
    icon_src = Image.open(icon_path).convert("RGBA")
    banner_src = Image.open(banner_path).convert("RGBA")
    
    # 1. Mipmap Icon Densities
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    
    for folder, size in densities.items():
        target_dir = os.path.join(res_dir, folder)
        os.makedirs(target_dir, exist_ok=True)
        
        # Rounded standard launcher
        icon_img = create_rounded_icon(icon_src, size)
        icon_img.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")
        
        # Round launcher
        round_img = create_circular_icon(icon_src, size)
        round_img.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder}/ic_launcher.png ({size}x{size})")

    # 2. Android TV Leanback Banner (16:9 ratio)
    # Standard: 320x180 (mdpi), 640x360 (xhdpi), 960x540 (xxhdpi)
    drawable_dir = os.path.join(res_dir, "drawable")
    drawable_xhdpi = os.path.join(res_dir, "drawable-xhdpi")
    os.makedirs(drawable_dir, exist_ok=True)
    os.makedirs(drawable_xhdpi, exist_ok=True)
    
    # Remove old vector xml if exists so png takes priority or is clean
    old_xml = os.path.join(drawable_dir, "tv_banner.xml")
    if os.path.exists(old_xml):
        os.remove(old_xml)
        
    old_icon_xml = os.path.join(res_dir, "mipmap-anydpi-v26", "ic_launcher.xml")
    if os.path.exists(old_icon_xml):
        os.remove(old_icon_xml)
    
    b320 = banner_src.resize((320, 180), Image.Resampling.LANCZOS)
    b320.save(os.path.join(drawable_dir, "tv_banner.png"), "PNG")
    
    b640 = banner_src.resize((640, 360), Image.Resampling.LANCZOS)
    b640.save(os.path.join(drawable_xhdpi, "tv_banner.png"), "PNG")
    
    print("Generated TV Banners: drawable/tv_banner.png (320x180) & drawable-xhdpi/tv_banner.png (640x360)")

if __name__ == "__main__":
    import sys
    base_dir = "/Users/hoanng/AIAgent/LiveSport"
    icon = os.path.join(base_dir, "art", "livesport77_icon_3d.png")
    banner = os.path.join(base_dir, "art", "livesport77_banner_3d.png")
    res = os.path.join(base_dir, "app", "src", "main", "res")
    if len(sys.argv) > 1:
        icon = sys.argv[1]
    if len(sys.argv) > 2:
        banner = sys.argv[2]
    process_assets(icon, banner, res)
