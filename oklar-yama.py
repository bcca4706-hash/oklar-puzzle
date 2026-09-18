#!/usr/bin/env python3
# OKLAR yama betigi - Termux icin
# Kullanim:  cd ~/oklar-puzzle && python3 oklar-yama.py
# Geri alma: git checkout index.html

import os, re, sys, shutil, subprocess

ROOT = os.getcwd()
IDX  = os.path.join(ROOT, "index.html")

if not os.path.isfile(IDX):
    sys.exit("HATA: index.html bulunamadi. Repo kokunde calistir: cd ~/oklar-puzzle")

src = open(IDX, encoding="utf-8").read()
shutil.copyfile(IDX, IDX + ".yedek")
uygulanan = []

def degistir(ad, eski, yeni):
    global src
    n = src.count(eski)
    if n == 0:
        print("  ATLANDI  " + ad + " (zaten uygulanmis olabilir)")
        return False
    if n > 1:
        sys.exit("HATA: " + ad + " icin " + str(n) + " eslesme var, tek olmali")
    src = src.replace(eski, yeni, 1)
    uygulanan.append(ad)
    print("  TAMAM    " + ad)
    return True

print("index.html yamalaniyor")

# 1 - LIG butonu ingilizce cevirisi
if 'ligT").textContent="\U0001F3C6 LEAGUE"' in src:
    print("  ATLANDI  1 LIG cevirisi (zaten ekli)")
else:
  degistir("1 LIG cevirisi",
    '  $("mSetBtn").textContent="\u2699\ufe0f SETTINGS";',
    '  $("mSetBtn").textContent="\u2699\ufe0f SETTINGS";\n'
    '  $("mLigBtn").textContent="\U0001F3C6 LEAGUE";\n'
    '  $("ligT").textContent="\U0001F3C6 LEAGUE";')

# 2 - egitimi atlayan kullanici dogrudan oyuna girsin
degistir("2 tutorial atlama",
  '$("tutSkip").onclick=function(){ sfx.tick(); tutKapat(); };',
  '$("tutSkip").onclick=function(){\n'
  '  sfx.tick(); tutKapat();\n'
  '  $("startOv").classList.add("hide");\n'
  '  startLevel(mem.level); startMusic();\n'
  '};')

# 3 - oyundan once takma ad sorulmasin (LIG icinde sorulmaya devam eder)
degistir("3 nick zorunlulugu",
  '  if(!mem.nick){ openNick(proceedToDaily); return; }\n', '')

# 4 - at siluetinin gercek boyutu 32x32
degistir("4 at siluet boyutu",
  '  at:{w:36,h:29,hex:', '  at:{w:32,h:32,hex:')

# 5 - bozuk siluetleri havuzdan cikar
degistir("5 bozuk siluet filtresi",
  'const IMG_SHAPE_KEYS=Object.keys(IMG_SHAPES);',
  'const BOZUK_SILUET=["astronot","denizalti","ejderha","futbol_topu",\n'
  '  "hazine_sandigi","kale","kartal","kelebek","kopekbaligi","kupa",\n'
  '  "kurt","oyun_kolu","pusula","robot","savas_ucagi","tank","ufo"];\n'
  'const IMG_SHAPE_KEYS=Object.keys(IMG_SHAPES)\n'
  '  .filter(function(k){return BOZUK_SILUET.indexOf(k)<0;});')

# 6 - 12 yeni siluet
YENI = """  sonsuz:{w:44,h:26,hex:"00000000000000000000000018000180001ff801ff8007ffc03ffe00ffff0ffff01ffff9ffff83ffff9ffffc3f01fff80fc7e007fe007e7c007fe003e7c003fc003e7c003fc003e7c003fc003e7c003fc003e7c007fe003e7e007fe007e3f01fff80fc3ffff9ffffc1ffff9ffff80ffff0ffff007ffc03ffe001ff801ff80001800018000000000000000000000000"},
  mum:{w:30,h:46,hex:"00000000000000000030000001e000000fc000003f000001fe000007f800001fe000003f000000fc000003f00000078000000c00000030000000c0000003000003fff0001fffe0007fffc00180070006001c001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001ffff0007fffc001fffe0007fff80000000000000000"},
  anahtar:{w:26,h:44,hex:"000000000fc0000ffc0007ff8003fff001fcfe007c0f803e01f00f807c03e01f00f807c03e01f00fc0fc01f03e007f3f800fffc001ffe0001fe00001e000007800001e000007800001e000007800001e000007ff8001ffe0007ff8001ffe0007ff8001e000007800001e000007800001ff80007fe0001ff80007fe0001ff80007800001e000007800001e000000000"},
  fincan:{w:40,h:32,hex:"0000000000000000000000000000000000000000000000000000000000000fffff80001fffffc0003fffffc0003fffffe0003fffffe7c03ffffffff03ffffffff83ffffffffc3ffffff83e3ffffff01e3fffffe01e3fffffe01e3ffffff01e3ffffff83e3ffffffffc3ffffffff83ffffffff03fffffe7c03fffffe0003fffffe0003fffffe0003fffffe0001fffffc0001fffff800000000000000000000000"},
  ampul:{w:30,h:44,hex:"00000000003f000003ff00003fff0001fffe000ffffc003ffff801ffffe007ffffc03fffff00fffffc07fffff81fffffe07fffff81fffffe07fffff81fffffe07fffff81fffffe07fffff80fffffc03fffff00fffffc01ffffe003ffff800ffffc001fffe0003fff00007ff80001ffe00007ff80001ffe00007ff80001ffe00003ff00000ffc00003ff0000000000003ff00000ffc00003ff00000ffc00001fe0000000000"},
  kalp2:{w:40,h:38,hex:"0000000000000000000000fc003f0003ff00ffc00fff81fff01fffc3fff83fffe7fffc3ffffffffc7ffffffffe7ffffffffe7ffffffffe7ffffffffe7ffffffffe7ffffffffe7ffffffffe7ffffffffe3ffffffffc1ffffffff80ffffffff007ffffffe003ffffffc001ffffff8000ffffff00007ffffe00007ffffe00003ffffc00001ffff800000ffff0000007ffe0000003ffe0000003ffc0000001ff80000000ff000000007e000000003c000000003c000000001800000000000000"},
  yildiz2:{w:40,h:38,hex:"00000000000000180000000018000000001c000000003c000000003c000000007e000000007e000000007e00000000ff00000000ff00000001ff80000001ff80007ffffffffe1ffffffff80ffffffff007ffffffe001ffffff8000ffffff00007ffffe00001ffff800000ffff000000ffff000001ffff800001ffff800001ffff800003ffffc00003fe7fc00003fc3fc00003f00fc00007e007e000078001e00006000060000c00003000000000000000000000000000000000000000000"},
  ay2:{w:36,h:38,hex:"000000000000080000000f80000003e0000000fe0000001fc0000003f80000007f8000000ff0000000ff0000001ff0000001fe0000003fe0000003fe0000003fe0000003fe0000007fe0000007fe0000007fe0000007fe0000007fe0000007fe0000003ff0000003ff0000003ff0000003ff8000001ff8000001ffc000000ffe000000fff0000007ff8000003ffc000001ffe000000fff8000003fff000000ffe0000000e0000000000000"},
  ev:{w:40,h:36,hex:"0000000000000000000000003c000000007e00000000ff00000003ffc0000007ffe000000ffff000003ffffc00007ffffe0000ffffff0003ffffffc007ffffffe00ffffffff03ffffffffc7ffffffffe07ffffffe007ffffffe007ffffffe007ffffffe007ffffffe007ffffffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe007ff00ffe00000000000"},
  roket2:{w:34,h:44,hex:"000000000000100000000c0000000780000001e0000000fc0000003f8000001fe000000ffc000003ff000001ffe00000fffc00003fff00001fcfe00007e1f80001f03e00007c0f80001f03e00007c0f80001f03e00007c0f80001f87e00007fff80001fffe0000ffffc0007ffff8001ffffe000fffffc003fffff001fffffe007fffff803ffffff01fcffcfe07f1fe3f83f87f87f0c00fc00c0003f00000007c0000001e0000000780000000c00000003000000000000000000000"},
  elmas2:{w:36,h:40,hex:"00000000000000000000fffff0001fffff8003fffffc003fffffc007fffffe00fffffff00fffffff01fffffff81fffffff83fffffffc7fffffffe7fffffffe7fffffffe3fffffffc1fffffff81fffffff80fffffff007fffffe007fffffe003fffffc001fffff8000fffff8000fffff00007fffe00003fffc00003fffc00001fff800000fff000000fff0000007fe0000003fc0000003fc0000001f80000000f0000000070000000060000000000000000000000"},
  semsiye:{w:40,h:40,hex:"0000000000000000000000007e00000007ffe000003ffffc0000ffffff0001ffffff8003ffffffc007ffffffe00ffffffff01ffffffff81ffffffff83ffffffffc3ffffffffc7ffffffffe7ffffffffe7ffffffffe7ffffffffe7ffffffffe00003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000003c000000009900000001c380000000c300000000e7000000007e000000001800000000000000"},
"""
if "sonsuz:{w:44" in src:
    print("  ATLANDI  6 yeni siluetler (zaten ekli)")
else:
    degistir("6 yeni siluetler", "IMG_SHAPES_RAW={\n", "IMG_SHAPES_RAW={\n" + YENI)

# 7 - izgara tavanini kademeli buyut
degistir("7 izgara cozunurlugu",
  '  }else{                           // uzman: tam izgara, en yogun\n'
  '    cols=20; rows=34; fill=0.97;\n'
  '  }',
  '  }else{                           // uzman: kademeli buyume, ust sinir 32x52\n'
  '    const t=Math.min(1,(n-100)/400);\n'
  '    cols=20+Math.round(t*12); rows=34+Math.round(t*18); fill=0.92+t*0.05;\n'
  '  }')

if not uygulanan:
    sys.exit("Hicbir yama uygulanmadi, dosya degismedi.")

open(IDX, "w", encoding="utf-8").write(src)

# --- surum numarasi ---
grad = None
for yol in ["android/app/build.gradle", "android/app/build.gradle.kts"]:
    if os.path.isfile(os.path.join(ROOT, yol)):
        grad = os.path.join(ROOT, yol); break

yeni_vc = None
if grad:
    g = open(grad, encoding="utf-8").read()
    m = re.search(r"versionCode\s+(\d+)", g)
    if m:
        yeni_vc = int(m.group(1)) + 1
        g = g[:m.start()] + "versionCode " + str(yeni_vc) + g[m.end():]
    m2 = re.search(r'versionName\s+"(\d+)\.(\d+)(\.\d+)?"', g)
    if m2:
        yeni_vn = m2.group(1) + "." + str(int(m2.group(2)) + 1)
        g = g[:m2.start()] + 'versionName "' + yeni_vn + '"' + g[m2.end():]
        s2 = open(IDX, encoding="utf-8").read()
        s2 = re.sub(r'\$\("setVer"\)\.textContent="OKLAR v[\d.]+";',
                    '$("setVer").textContent="OKLAR v' + yeni_vn + '";', s2)
        open(IDX, "w", encoding="utf-8").write(s2)
        print("  surum     " + m2.group(0).split('"')[1] + " -> " + yeni_vn)
    open(grad, "w", encoding="utf-8").write(g)
    if yeni_vc:
        print("  versionCode -> " + str(yeni_vc))
else:
    print("  UYARI: build.gradle bulunamadi, versionCode elle artirilmali")

# --- dogrulama ---
print("\nDogrulama")
s3 = open(IDX, encoding="utf-8").read()
kontrol = [
    ("LEAGUE cevirisi", 'ligT").textContent="\U0001F3C6 LEAGUE"'),
    ("tutorial atlama", 'tutKapat();\n  $("startOv")'),
    ("bozuk filtre",    "BOZUK_SILUET"),
    ("yeni siluet",     "sonsuz:{w:44"),
    ("izgara buyume",   "cols=20+Math.round(t*12)"),
]
hepsi = True
for ad, ipucu in kontrol:
    v = ipucu in s3
    hepsi = hepsi and v
    print(("  var  " if v else "  YOK  ") + ad)
if "if(!mem.nick){ openNick(proceedToDaily)" in s3:
    print("  YOK  nick kaldirma"); hepsi = False
else:
    print("  var  nick kaldirma")

if not hepsi:
    sys.exit("\nBazi yamalar uygulanmadi. index.html.yedek ile karsilastir.")

print("\nTum yamalar uygulandi.")

# --- git ---
if "--push" in sys.argv:
    mesaj = "fix: siluet verisi, izgara cozunurlugu, ilk oturum surtunmesi"
    if yeni_vc: mesaj += " (vc " + str(yeni_vc) + ")"
    subprocess.run(["git", "add", "-A"], check=True)
    subprocess.run(["git", "commit", "-m", mesaj], check=True)
    subprocess.run(["git", "push"], check=True)
    print("\nPush tamam. GitHub Actions AAB uretecek.")
else:
    print("\nGondermek icin:  python3 oklar-yama.py --push")
    print("veya elle:       git add -A && git commit -m 'yama' && git push")
