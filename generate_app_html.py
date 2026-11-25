#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import os
import glob
import re
import base64

DATA_DIR = 'app/src/main/assets/data'

def load_json(name):
    with open(f'{DATA_DIR}/{name}', 'r', encoding='utf-8') as f:
        return json.load(f)

all_items = load_json('all_items.json')
cd_tracks = load_json('cd_tracks.json')
grammar_lessons = load_json('grammar_lessons.json')
hiragana_items = load_json('hiragana_items.json')
katakana_items = load_json('katakana_items.json')
interview_items = load_json('interview_items.json')
particle_rules = load_json('particle_rules.json')
particle_questions = load_json('particle_questions.json')

# Map CD number to its public asset path
cd_asset_map = {}
pattern = re.compile(r'CD_(\d+)_([a-f0-9]+)\.mp3')
for f in glob.glob('public/assets/aistudio/CD_*.mp3'):
    m = pattern.search(f)
    if m:
        cd_asset_map[int(m.group(1))] = f

with open('app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png', 'rb') as _icon_f:
    app_icon_b64 = base64.b64encode(_icon_f.read()).decode('ascii')

html_template = '''<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <title>Japanese Dopamine - JLPT N5 Master</title>
  <link rel="icon" type="image/png" href="data:image/png;base64,''' + app_icon_b64 + '''">
  <link rel="apple-touch-icon" href="data:image/png;base64,''' + app_icon_b64 + '''">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Noto+Sans+Bengali:wght@400;500;600;700;800&family=Noto+Sans+JP:wght@400;500;700;900&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #09080E;
      --card-bg: #14121B;
      --card-elevated: #1E1A2B;
      --card-border: #2C253D;
      --primary: #FF2E55;
      --primary-glow: rgba(255, 46, 85, 0.25);
      --accent-yellow: #FFA114;
      --accent-green: #00E676;
      --accent-cyan: #00E5FF;
      --accent-violet: #B388FF;
      --accent-sakura: #FF699A;
      --accent-coral: #FF8A65;
      --text-main: #F7F6FC;
      --text-muted: #A8A1B8;
    }

    body.light-theme {
      --bg: #F4F5F8;
      --card-bg: #FFFFFF;
      --card-elevated: #F0F2F6;
      --card-border: #E2E6EE;
      --primary: #E60039;
      --primary-glow: rgba(230, 0, 57, 0.15);
      --accent-yellow: #D97706;
      --accent-green: #059669;
      --accent-cyan: #0284C7;
      --accent-violet: #7C3AED;
      --accent-sakura: #DB2777;
      --accent-coral: #EA580C;
      --text-main: #0F172A;
      --text-muted: #64748B;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
      user-select: none;
    }

    body {
      background-color: var(--bg);
      color: var(--text-main);
      font-family: 'Outfit', 'Noto Sans Bengali', 'Noto Sans JP', sans-serif;
      min-height: 100vh;
      display: flex;
      justify-content: center;
      transition: background-color 0.3s ease, color 0.3s ease;
      overflow-x: hidden;
    }

    #appContainer {
      width: 100%;
      max-width: 600px;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      position: relative;
      background: var(--bg);
      box-shadow: 0 0 50px rgba(0,0,0,0.5);
    }

    /* Screen management */
    .screen {
      display: none;
      flex-direction: column;
      width: 100%;
      min-height: 100vh;
      padding-bottom: 40px;
      position: relative;
      z-index: 1;
    }

    .screen.active {
      display: flex;
    }

    /* Top Bar */
    .app-topbar {
      position: sticky;
      top: 0;
      z-index: 100;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      background: rgba(14, 12, 22, 0.85);
      backdrop-filter: blur(14px);
      border-bottom: 1px solid var(--card-border);
    }

    body.light-theme .app-topbar {
      background: rgba(255, 255, 255, 0.9);
      border-bottom-color: var(--card-border);
    }

    .topbar-title-box {
      display: flex;
      flex-direction: column;
    }

    .topbar-title {
      font-size: 18px;
      font-weight: 800;
      color: var(--text-main);
      line-height: 1.2;
    }

    .topbar-subtitle {
      font-size: 11.5px;
      color: var(--accent-yellow);
      font-weight: 600;
    }

    .topbar-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--text-main);
      font-size: 18px;
      cursor: pointer;
      transition: transform 0.15s ease, background 0.2s ease;
    }

    .topbar-btn:active {
      transform: scale(0.92);
      background: var(--primary);
      color: #fff;
    }

    /* Header Profile & Greeting */
    .welcome-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 16px 8px 16px;
    }

    .badge-pill {
      font-size: 10px;
      font-weight: 800;
      padding: 2px 8px;
      border-radius: 6px;
      background: rgba(255, 161, 20, 0.15);
      color: var(--accent-yellow);
      border: 1px solid rgba(255, 161, 20, 0.3);
      display: inline-block;
      margin-bottom: 4px;
    }

    .welcome-name {
      font-size: 22px;
      font-weight: 900;
      color: var(--text-main);
    }

    .avatar-btn {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: var(--card-elevated);
      border: 1.5px solid var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 900;
      font-size: 18px;
      color: var(--primary);
      box-shadow: 0 0 15px var(--primary-glow);
      cursor: pointer;
    }

    /* Countdown Card */
    .countdown-card {
      margin: 8px 16px 12px 16px;
      border-radius: 20px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      padding: 16px;
      position: relative;
      overflow: hidden;
      background: linear-gradient(135deg, rgba(255, 46, 85, 0.08) 0%, var(--card-bg) 60%);
    }

    .countdown-watermark {
      position: absolute;
      right: -8px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 72px;
      font-weight: 900;
      color: rgba(255, 46, 85, 0.05);
      pointer-events: none;
    }

    .countdown-grid {
      display: flex;
      justify-content: space-between;
      margin-top: 10px;
      gap: 8px;
    }

    .countdown-box {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      background: var(--card-elevated);
      border-radius: 12px;
      padding: 8px 4px;
      border: 1px solid rgba(255,255,255,0.06);
    }

    .countdown-num {
      font-size: 20px;
      font-weight: 900;
      line-height: 1.1;
    }

    .countdown-label {
      font-size: 9px;
      font-weight: 800;
      color: var(--text-muted);
      letter-spacing: 0.5px;
      margin-top: 2px;
    }

    /* Stats Strip */
    .stats-strip {
      display: flex;
      gap: 10px;
      padding: 0 16px 12px 16px;
    }

    .stat-pill {
      flex: 1;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 10px 12px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 66px;
    }

    .stat-title {
      font-size: 9.5px;
      font-weight: 800;
      color: var(--text-muted);
      letter-spacing: 0.5px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .stat-val {
      font-size: 16px;
      font-weight: 900;
      color: var(--text-main);
    }

    /* Section Headers */
    .section-header {
      padding: 10px 16px 8px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .section-title {
      font-size: 12.5px;
      font-weight: 800;
      color: var(--text-muted);
      letter-spacing: 1px;
      text-transform: uppercase;
    }

    .section-count {
      font-size: 11px;
      font-weight: 700;
      color: var(--accent-yellow);
    }

    /* Module Cards Grid */
    .modules-container {
      padding: 0 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    /* Hero Module Card (1. হিরাগানা কাতাকানা) */
    .hero-module-card {
      background: linear-gradient(135deg, var(--card-elevated) 0%, var(--card-bg) 100%);
      border: 1.5px solid rgba(0, 230, 118, 0.45);
      border-radius: 22px;
      padding: 18px;
      position: relative;
      cursor: pointer;
      display: flex;
      align-items: center;
      overflow: hidden;
      box-shadow: 0 4px 20px rgba(0, 230, 118, 0.1);
      transition: transform 0.2s ease, border-color 0.2s ease;
    }

    .hero-module-card:active {
      transform: scale(0.98);
      border-color: var(--accent-green);
    }

    .hero-watermark {
      position: absolute;
      right: 12px;
      top: -6px;
      font-size: 52px;
      font-weight: 900;
      color: rgba(0, 230, 118, 0.12);
      pointer-events: none;
    }

    .hero-icon-box {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      background: rgba(0, 230, 118, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30px;
      font-weight: 900;
      color: var(--accent-green);
      flex-shrink: 0;
      margin-right: 14px;
    }

    .hero-content {
      flex: 1;
    }

    .hero-badge {
      display: inline-block;
      font-size: 10px;
      font-weight: 800;
      color: var(--accent-green);
      background: rgba(0, 230, 118, 0.15);
      padding: 2px 8px;
      border-radius: 6px;
      margin-bottom: 4px;
    }

    .hero-title {
      font-size: 18px;
      font-weight: 900;
      color: var(--text-main);
      line-height: 1.2;
    }

    .hero-sub {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 2px;
    }

    .hero-arrow {
      font-size: 20px;
      color: var(--accent-green);
      margin-left: 10px;
    }

    /* 2-column Grid for Modules 2 to 7 */
    .modules-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .module-card {
      background: linear-gradient(135deg, var(--card-elevated) 0%, var(--card-bg) 100%);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 14px;
      height: 136px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s ease, border-color 0.2s ease;
    }

    .module-card:active {
      transform: scale(0.97);
    }

    .card-watermark {
      position: absolute;
      right: 6px;
      bottom: 4px;
      font-size: 54px;
      font-weight: 900;
      opacity: 0.12;
      pointer-events: none;
    }

    .module-top-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      z-index: 1;
    }

    .module-glyph-box {
      width: 34px;
      height: 34px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 17px;
      font-weight: 900;
    }

    .module-badge-tag {
      font-size: 10px;
      font-weight: 800;
      padding: 2px 6px;
      border-radius: 6px;
    }

    .module-info {
      z-index: 1;
    }

    .module-title {
      font-size: 15px;
      font-weight: 800;
      color: var(--text-main);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .module-english {
      font-size: 11px;
      font-weight: 600;
      margin-top: 1px;
    }

    .module-sub {
      font-size: 10.5px;
      color: var(--text-muted);
      margin-top: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    /* 8. Bookmarks Bar */
    .bookmarks-bar {
      background: var(--card-bg);
      border: 1px solid rgba(255, 161, 20, 0.4);
      border-radius: 18px;
      padding: 14px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      cursor: pointer;
    }

    .bookmarks-bar:active {
      transform: scale(0.98);
    }

    /* Common Components inside Screens */
    .search-input-box {
      padding: 10px 16px;
    }

    .search-input {
      width: 100%;
      height: 46px;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 0 16px;
      color: var(--text-main);
      font-size: 14px;
      outline: none;
      font-family: inherit;
    }

    .search-input:focus {
      border-color: var(--primary);
    }

    /* Scrollable Chip Row */
    .chip-scroll-row {
      display: flex;
      gap: 8px;
      padding: 0 16px 10px 16px;
      overflow-x: auto;
      scrollbar-width: none;
    }

    .chip-scroll-row::-webkit-scrollbar {
      display: none;
    }

    .chip-btn {
      flex-shrink: 0;
      padding: 6px 14px;
      border-radius: 10px;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--text-muted);
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .chip-btn.active {
      background: var(--primary);
      color: #fff;
      border-color: var(--primary);
      box-shadow: 0 2px 10px var(--primary-glow);
    }

    /* Item Card Lists */
    .items-list {
      padding: 0 16px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .item-row-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 14px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    .item-main-content {
      flex: 1;
    }

    .item-jp-text {
      font-size: 20px;
      font-weight: 900;
      color: var(--text-main);
    }

    .item-romaji-text {
      font-size: 12px;
      font-weight: 600;
      color: var(--accent-yellow);
      margin-top: 1px;
    }

    .item-bn-text {
      font-size: 14px;
      font-weight: 600;
      color: var(--accent-green);
      margin-top: 2px;
    }

    .item-en-text {
      font-size: 11px;
      color: var(--text-muted);
      margin-top: 1px;
    }

    .item-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .speaker-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--accent-yellow);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      cursor: pointer;
      transition: transform 0.15s ease, background 0.2s ease, color 0.2s ease;
    }

    .speaker-btn:active, .speaker-btn.playing {
      transform: scale(1.15);
      background: var(--primary);
      color: #fff;
      box-shadow: 0 0 15px var(--primary-glow);
    }

    .star-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--text-muted);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      cursor: pointer;
    }

    .star-btn.active {
      color: var(--accent-yellow);
      background: rgba(255, 161, 20, 0.15);
      border-color: var(--accent-yellow);
    }

    /* Listening CD Card */
    .cd-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      transition: border-color 0.2s ease;
    }

    .cd-card.playing {
      border-color: var(--primary);
      box-shadow: 0 4px 20px var(--primary-glow);
    }

    .cd-header-row {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .cd-disc-icon {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: var(--card-elevated);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      color: var(--accent-yellow);
      flex-shrink: 0;
    }

    .cd-card.playing .cd-disc-icon {
      background: rgba(255, 46, 85, 0.25);
      color: var(--primary);
      animation: spinDisc 3s linear infinite;
    }

    @keyframes spinDisc {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .cd-info-col {
      flex: 1;
    }

    .cd-title {
      font-size: 15px;
      font-weight: 800;
      color: var(--text-main);
    }

    .cd-subtitle {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 2px;
    }

    .cd-play-btn {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: var(--card-elevated);
      border: none;
      color: var(--accent-yellow);
      font-size: 22px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .cd-card.playing .cd-play-btn {
      background: var(--primary);
      color: #fff;
      box-shadow: 0 0 15px var(--primary-glow);
    }

    /* Player Seekbar & Controls */
    .cd-player-controls {
      display: none;
      flex-direction: column;
      gap: 8px;
      padding-top: 8px;
      border-top: 1px solid var(--card-border);
    }

    .cd-card.active-player .cd-player-controls {
      display: flex;
    }

    .seek-slider {
      width: 100%;
      accent-color: var(--primary);
      cursor: pointer;
    }

    .time-row {
      display: flex;
      justify-content: space-between;
      font-size: 11px;
      color: var(--text-muted);
      font-weight: 600;
    }

    .sub-controls-row {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      margin-top: 4px;
    }

    .sub-ctrl-btn {
      padding: 4px 10px;
      border-radius: 8px;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--text-main);
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
    }

    .sub-ctrl-btn:active {
      background: var(--primary);
      color: #fff;
    }

    /* Expandable Script Box */
    .script-toggle-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 0;
      color: var(--accent-green);
      font-size: 12px;
      font-weight: 700;
      cursor: pointer;
    }

    .script-content-box {
      display: none;
      flex-direction: column;
      gap: 8px;
      padding-top: 10px;
      border-top: 1px solid var(--card-border);
    }

    .script-content-box.expanded {
      display: flex;
    }

    .script-line-card {
      background: var(--card-elevated);
      border-radius: 12px;
      padding: 10px 12px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
    }

    .script-speaker {
      font-size: 11px;
      font-weight: 800;
      color: var(--accent-yellow);
    }

    .script-jp {
      font-size: 14px;
      font-weight: 700;
      color: var(--text-main);
      margin-top: 2px;
    }

    .script-romaji {
      font-size: 11.5px;
      color: var(--text-muted);
    }

    .script-bn {
      font-size: 12.5px;
      color: var(--accent-green);
      font-weight: 600;
      margin-top: 2px;
    }

    /* 3D Flashcard Modal / Overlay */
    .flashcard-modal-overlay {
      display: none;
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: var(--bg);
      z-index: 200;
      flex-direction: column;
      padding: 16px;
      justify-content: space-between;
    }

    .flashcard-modal-overlay.active {
      display: flex;
    }

    .fc-card-container {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      perspective: 1200px;
      margin: 16px 0;
    }

    .fc-3d-card {
      width: 100%;
      max-width: 480px;
      height: 380px;
      position: relative;
      transform-style: preserve-3d;
      transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      cursor: pointer;
    }

    .fc-3d-card.flipped {
      transform: rotateY(180deg);
    }

    .fc-face {
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 26px;
      background: linear-gradient(135deg, var(--card-elevated) 0%, var(--card-bg) 100%);
      border: 1.5px solid var(--primary);
      box-shadow: 0 10px 30px var(--primary-glow);
      backface-visibility: hidden;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 24px;
      text-align: center;
    }

    .fc-face.back {
      transform: rotateY(180deg);
      border-color: var(--accent-green);
      box-shadow: 0 10px 30px rgba(0, 230, 118, 0.2);
    }

    .fc-main-jp {
      font-size: 44px;
      font-weight: 900;
      color: var(--text-main);
      line-height: 1.2;
    }

    .fc-romaji {
      font-size: 20px;
      font-weight: 600;
      color: var(--accent-yellow);
      margin-top: 8px;
    }

    .fc-main-bn {
      font-size: 32px;
      font-weight: 800;
      color: var(--accent-green);
      line-height: 1.3;
    }

    .fc-bottom-hint {
      font-size: 12px;
      color: var(--text-muted);
      font-weight: 600;
      padding: 6px 12px;
      background: var(--card-bg);
      border-radius: 10px;
      border: 1px solid var(--card-border);
      align-self: center;
    }

    .fc-bottom-controls {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    /* Handwriting Canvas */
    .canvas-box {
      background: var(--card-elevated);
      border: 1.5px dashed var(--card-border);
      border-radius: 20px;
      margin: 12px 16px;
      position: relative;
      overflow: hidden;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 280px;
    }

    #drawingCanvas {
      width: 100%;
      height: 100%;
      touch-action: none;
      cursor: crosshair;
    }

    .canvas-watermark {
      position: absolute;
      font-size: 180px;
      font-weight: 900;
      color: rgba(255, 255, 255, 0.08);
      pointer-events: none;
      font-family: 'Noto Sans JP', sans-serif;
    }

    /* Quiz Modal */
    .quiz-modal-overlay {
      display: none;
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: var(--bg);
      z-index: 210;
      flex-direction: column;
      padding: 16px;
      justify-content: space-between;
    }

    .quiz-modal-overlay.active {
      display: flex;
    }

    .quiz-question-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 20px;
      padding: 24px;
      text-align: center;
      margin: 20px 0;
    }

    .quiz-prompt-text {
      font-size: 32px;
      font-weight: 900;
      color: var(--text-main);
    }

    .quiz-options-grid {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .quiz-opt-btn {
      width: 100%;
      padding: 14px 16px;
      border-radius: 14px;
      background: var(--card-elevated);
      border: 1.5px solid var(--card-border);
      color: var(--text-main);
      font-size: 15px;
      font-weight: 700;
      text-align: left;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .quiz-opt-btn:active {
      transform: scale(0.98);
    }

    .quiz-opt-btn.correct {
      background: rgba(0, 230, 118, 0.2);
      border-color: var(--accent-green);
      color: var(--accent-green);
    }

    .quiz-opt-btn.wrong {
      background: rgba(255, 46, 85, 0.2);
      border-color: var(--primary);
      color: var(--primary);
    }

    /* Hiragana/Katakana-Matched Dark Tile Style (No Red Border, Subtle Outline, Clean Typographic Hierarchy) */
    .stacked-box-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 10px;
      padding: 12px 14px 28px 14px;
    }

    .stacked-box-wrap {
      position: relative;
      aspect-ratio: 0.94;
      cursor: pointer;
    }

    .stacked-box-front {
      width: 100%;
      height: 100%;
      border-radius: 12px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      padding: 8px 6px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      text-align: center;
      transition: transform 0.15s ease, background 0.15s ease;
    }

    .stacked-box-wrap:active .stacked-box-front {
      transform: scale(0.96);
      background: var(--card-elevated);
    }

    .stacked-box-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 900;
      font-size: 22px;
      line-height: 1.1;
      color: var(--text-main);
    }

    .stacked-box-title {
      font-size: 12px;
      font-weight: 700;
      color: var(--primary);
      line-height: 1.25;
      word-break: break-word;
    }

    .stacked-box-badge {
      font-size: 10px;
      font-weight: 500;
      color: var(--text-muted);
      line-height: 1.2;
    }

    /* Video-Matched Ambient Bottom-Glowing Color-Shifting Gradient */
    #ambientAuraBg,
    .modal-aura-bg {
      position: fixed;
      top: 0;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 100%;
      max-width: 600px;
      pointer-events: none;
      z-index: 0;
      overflow: hidden;
    }

    #ambientAuraBg {
      display: none;
    }

    #ambientAuraBg.active {
      display: block;
    }

    body.light-theme #ambientAuraBg,
    body.light-theme .modal-aura-bg {
      opacity: 0.35;
    }

    #ambientAuraBg .aura-layer,
    .modal-aura-bg .aura-layer {
      position: absolute;
      inset: 0;
      will-change: opacity;
    }

    #ambientAuraBg .aura-purple,
    .modal-aura-bg .aura-purple {
      background:
        radial-gradient(circle 68vmax at 15% 96%, rgba(123, 44, 191, 0.34) 0%, rgba(123, 44, 191, 0.14) 38%, transparent 72%),
        radial-gradient(circle 68vmax at 85% 96%, rgba(255, 30, 184, 0.34) 0%, rgba(255, 30, 184, 0.14) 38%, transparent 72%);
      animation: auraFadePurple 4.2s linear infinite;
    }

    #ambientAuraBg .aura-orange,
    .modal-aura-bg .aura-orange {
      background:
        radial-gradient(circle 68vmax at 15% 96%, rgba(255, 109, 0, 0.34) 0%, rgba(255, 109, 0, 0.14) 38%, transparent 72%),
        radial-gradient(circle 68vmax at 85% 96%, rgba(174, 234, 0, 0.34) 0%, rgba(174, 234, 0, 0.14) 38%, transparent 72%);
      animation: auraFadeOrange 4.2s linear infinite;
    }

    #ambientAuraBg .aura-green,
    .modal-aura-bg .aura-green {
      background:
        radial-gradient(circle 68vmax at 15% 96%, rgba(0, 230, 118, 0.34) 0%, rgba(0, 230, 118, 0.14) 38%, transparent 72%),
        radial-gradient(circle 68vmax at 85% 96%, rgba(0, 184, 212, 0.34) 0%, rgba(0, 184, 212, 0.14) 38%, transparent 72%);
      animation: auraFadeGreen 4.2s linear infinite;
    }

    #flashcardModal > *:not(.modal-aura-bg),
    #quizModal > *:not(.modal-aura-bg) {
      position: relative;
      z-index: 1;
    }

    @keyframes auraFadePurple {
      0%, 100% { opacity: 1; }
      33.33% { opacity: 0; }
      66.66% { opacity: 0; }
    }

    @keyframes auraFadeOrange {
      0%, 100% { opacity: 0; }
      33.33% { opacity: 1; }
      66.66% { opacity: 0; }
    }

    @keyframes auraFadeGreen {
      0%, 100% { opacity: 0; }
      33.33% { opacity: 0; }
      66.66% { opacity: 1; }
    }

    .lesson-action-strip {
      display: flex;
      gap: 10px;
      padding: 10px 16px 4px 16px;
    }

    .lesson-action-btn {
      flex: 1;
      height: 46px;
      border-radius: 14px;
      background: var(--card-elevated);
      border: 1px solid var(--card-border);
      color: var(--text-main);
      font-size: 13px;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      cursor: pointer;
    }

    .lesson-action-btn:active {
      transform: scale(0.97);
    }
  </style>
</head>
<body>
  <div id="appContainer">
    <div id="ambientAuraBg">
      <div class="aura-layer aura-purple"></div>
      <div class="aura-layer aura-orange"></div>
      <div class="aura-layer aura-green"></div>
    </div>

    <!-- ============================================== -->
    <!-- SCREEN: DASHBOARD                              -->
    <!-- ============================================== -->
    <div id="screenDashboard" class="screen active">
      <div class="welcome-row">
        <div>
          <span class="badge-pill">日本語マスター • N5 PRO</span>
          <div class="welcome-name" id="userDisplayName">Japanese Learner</div>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <button class="topbar-btn" onclick="toggleTheme()" title="Theme Toggle">🌙</button>
          <div class="avatar-btn" onclick="openScreen('screenBookmarks')">★</div>
        </div>
      </div>

      <!-- Stats Strip -->
      <div class="stats-strip" style="margin-top: 10px;">
        <div class="stat-pill">
          <div class="stat-title"><span style="color:var(--accent-cyan)">⏱️</span> STUDY TIME</div>
          <div class="stat-val" id="statStudyTime">15m</div>
        </div>
        <div class="stat-pill">
          <div class="stat-title"><span style="color:var(--accent-yellow)">🎯</span> QUIZZES</div>
          <div class="stat-val" id="statQuizzes">0</div>
        </div>
        <div class="stat-pill">
          <div class="stat-title"><span style="color:var(--accent-green)">⚡</span> ACCURACY</div>
          <div class="stat-val" id="statAccuracy">100%</div>
        </div>
      </div>

      <!-- Section Title -->
      <div class="section-header">
        <span class="section-title">STUDY MODULES</span>
        <span class="section-count">8 Modules</span>
      </div>

      <!-- Modules List (In exact sequence) -->
      <div class="modules-container">

        <!-- 1. Hiragana Katakana (Hero Card) -->
        <div class="hero-module-card" onclick="openScreen('screenKana')">
          <div class="hero-watermark">あ・ア</div>
          <div class="hero-icon-box">あ</div>
          <div class="hero-content">
            <span class="hero-badge">FOUNDATION • 92 LETTERS</span>
            <div class="hero-title">Hiragana Katakana</div>
          </div>
          <div class="hero-arrow">➔</div>
        </div>

        <!-- 2x2 Grid for Modules 2 to 7 -->
        <div class="modules-grid">

          <!-- 2. Vocabulary -->
          <div class="module-card" onclick="openScreen('screenVocab')" style="border-color:rgba(255, 46, 85, 0.4);">
            <div class="card-watermark" style="color:var(--primary);">語</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(255, 46, 85, 0.18); color:var(--primary);">語</div>
              <span class="module-badge-tag" style="background:rgba(255, 46, 85, 0.15); color:var(--primary);">25 Lessons</span>
            </div>
            <div class="module-info">
              <div class="module-title">Vocabulary</div>
            </div>
          </div>

          <!-- 3. Grammar -->
          <div class="module-card" onclick="openScreen('screenGrammar')" style="border-color:rgba(0, 229, 255, 0.4);">
            <div class="card-watermark" style="color:var(--accent-cyan);">文</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(0, 229, 255, 0.18); color:var(--accent-cyan);">文</div>
              <span class="module-badge-tag" style="background:rgba(0, 229, 255, 0.15); color:var(--accent-cyan);">21 Lessons</span>
            </div>
            <div class="module-info">
              <div class="module-title">Grammar</div>
            </div>
          </div>

          <!-- 4. Kanji -->
          <div class="module-card" onclick="openScreen('screenKanji')" style="border-color:rgba(255, 161, 20, 0.4);">
            <div class="card-watermark" style="color:var(--accent-yellow);">漢</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(255, 161, 20, 0.18); color:var(--accent-yellow);">漢</div>
              <span class="module-badge-tag" style="background:rgba(255, 161, 20, 0.15); color:var(--accent-yellow);">110 N5</span>
            </div>
            <div class="module-info">
              <div class="module-title">Kanji</div>
            </div>
          </div>

          <!-- 5. Listening -->
          <div class="module-card" onclick="openScreen('screenListening')" style="border-color:rgba(179, 136, 255, 0.4);">
            <div class="card-watermark" style="color:var(--accent-violet);">聴</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(179, 136, 255, 0.18); color:var(--accent-violet);">聴</div>
              <span class="module-badge-tag" style="background:rgba(179, 136, 255, 0.15); color:var(--accent-violet);">87 CDs</span>
            </div>
            <div class="module-info">
              <div class="module-title">Listening</div>
            </div>
          </div>

          <!-- 6. Interview -->
          <div class="module-card" onclick="openScreen('screenInterview')" style="border-color:rgba(255, 105, 154, 0.4);">
            <div class="card-watermark" style="color:var(--accent-sakura);">問</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(255, 105, 154, 0.18); color:var(--accent-sakura);">問</div>
              <span class="module-badge-tag" style="background:rgba(255, 105, 154, 0.15); color:var(--accent-sakura);">194 Q&A</span>
            </div>
            <div class="module-info">
              <div class="module-title">Interview</div>
            </div>
          </div>

          <!-- 7. Counting Units -->
          <div class="module-card" onclick="openScreen('screenCounting')" style="border-color:rgba(255, 138, 101, 0.4);">
            <div class="card-watermark" style="color:var(--accent-coral);">数</div>
            <div class="module-top-row">
              <div class="module-glyph-box" style="background:rgba(255, 138, 101, 0.18); color:var(--accent-coral);">数</div>
              <span class="module-badge-tag" style="background:rgba(255, 138, 101, 0.15); color:var(--accent-coral);">18 Units</span>
            </div>
            <div class="module-info">
              <div class="module-title">Counting Units</div>
            </div>
          </div>

        </div>

        <!-- 8. Bookmarks -->
        <div class="bookmarks-bar" onclick="openScreen('screenBookmarks')">
          <div style="display:flex; align-items:center; gap:12px;">
            <div style="width:40px; height:40px; border-radius:50%; background:rgba(255, 161, 20, 0.18); display:flex; align-items:center; justify-content:center; color:var(--accent-yellow); font-size:20px;">★</div>
            <div style="font-size:16px; font-weight:800; color:var(--text-main);">Bookmarks</div>
          </div>
          <div style="color:var(--accent-yellow); font-size:18px;">➔</div>
        </div>

      </div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 1: KANA                                 -->
    <!-- ============================================== -->
    <div id="screenKana" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Hiragana Katakana</div>
          <div class="topbar-subtitle">Handwriting Practice & Audio Chart</div>
        </div>
        <div style="width:40px;"></div>
      </div>

      <div class="chip-scroll-row" style="margin-top:12px;">
        <button class="chip-btn active" id="tabKanaHira" onclick="switchKanaTab('HIRAGANA')">Hiragana (ひらがな)</button>
        <button class="chip-btn" id="tabKanaKata" onclick="switchKanaTab('KATAKANA')">Katakana (カタカナ)</button>
        <button class="chip-btn" id="tabKanaCanvas" onclick="switchKanaTab('CANVAS')">Handwriting ✍️</button>
      </div>

      <div id="kanaGridContainer" class="items-list" style="display:grid; grid-template-columns: repeat(4, 1fr); gap:8px;"></div>

      <div id="kanaCanvasContainer" style="display:none; flex-direction:column;">
        <div style="text-align:center; padding:10px 16px 4px 16px;">
          <div style="font-size:14px; font-weight:700; color:var(--accent-yellow);" id="currentTracingLabel">あ (a) - Trace & Write</div>
          <div style="font-size:11.5px; color:var(--text-muted);">Trace inside the box</div>
        </div>
        <div class="canvas-box">
          <div class="canvas-watermark" id="canvasWatermarkChar">あ</div>
          <canvas id="drawingCanvas"></canvas>
        </div>
        <div style="display:flex; justify-content:center; gap:12px; padding:0 16px;">
          <button class="chip-btn" onclick="clearCanvas()">Clear</button>
          <button class="chip-btn" onclick="speakCurrentKana()">Pronounce 🔊</button>
          <button class="chip-btn active" onclick="nextPracticeKana()">Next ➔</button>
        </div>
      </div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 2: VOCABULARY                           -->
    <!-- ============================================== -->
    <div id="screenVocab" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="backFromVocab()">←</button>
        <div class="topbar-title-box" style="align-items:center; text-align:center; flex:1;">
          <div class="topbar-title" id="vocabTopTitle">Vocabulary</div>
          <div class="topbar-subtitle" id="vocabTopSubtitle" style="display:none;"></div>
        </div>
        <div style="width:40px;"></div>
      </div>

      <div id="vocabGridContainer" class="stacked-box-grid"></div>

      <div id="vocabDetailContainer" style="display:none; flex-direction:column;">
        <div class="lesson-action-strip">
          <button class="lesson-action-btn" style="border-color:rgba(255, 161, 20, 0.5);" onclick="openFlashcards('VOCABULARY')">
            <span style="color:var(--accent-yellow);">🗂️</span>
            <span id="vocabFlashcardBtnText">Flashcards</span>
          </button>
          <button class="lesson-action-btn" style="border-color:rgba(0, 230, 118, 0.5);" onclick="openQuiz('VOCABULARY')">
            <span style="color:var(--accent-green);">🎮</span>
            <span id="vocabQuizBtnText">Take Quiz</span>
          </button>
        </div>

        <div class="search-input-box">
          <input type="text" class="search-input" id="vocabSearchInput" placeholder="Search Japanese, Romaji, or Bangla..." oninput="renderVocabList()">
        </div>

        <div id="vocabLessonCountLabel" style="padding:0 16px 8px 16px; font-size:11px; font-weight:800; color:var(--accent-yellow); letter-spacing:0.8px;"></div>
        <div class="items-list" id="vocabItemsList"></div>
      </div>

      <div id="vocabMasterQuizPlaceholder" style="display:none; padding:24px;">
        <div class="item-row-card" style="flex-direction:column; align-items:center; text-align:center; padding:28px; gap:12px;">
          <div style="width:60px; height:60px; border-radius:50%; background:rgba(255, 161, 20, 0.16); display:flex; align-items:center; justify-content:center; font-size:28px;">📝</div>
          <div style="font-size:20px; font-weight:900; color:var(--text-main);">Vocabulary Quiz</div>
          <div style="font-size:13.5px; color:var(--text-muted); line-height:1.5;">All 25 Lessons Combined Quiz<br>(Content will be added here later)</div>
          <button class="chip-btn active" style="margin-top:8px; padding:10px 20px;" onclick="backFromVocab()">Back to Lessons</button>
        </div>
      </div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 3: GRAMMAR                              -->
    <!-- ============================================== -->
    <div id="screenGrammar" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="backFromGrammar()">←</button>
        <div class="topbar-title-box" style="align-items:center; text-align:center; flex:1;">
          <div class="topbar-title" id="grammarTopTitle">Grammar</div>
          <div class="topbar-subtitle" id="grammarTopSubtitle" style="display:none;"></div>
        </div>
        <button class="topbar-btn" id="grammarTopQuizBtn" onclick="openParticleQuiz()" title="Particle Quiz">📝</button>
      </div>

      <div class="chip-scroll-row" id="grammarTabRow" style="margin-top:12px;">
        <button class="chip-btn active" id="tabGrammarLessons" onclick="switchGrammarTab('LESSONS')">21 Lessons</button>
        <button class="chip-btn" id="tabGrammarParticles" onclick="switchGrammarTab('PARTICLES')">28 Particles</button>
      </div>

      <div id="grammarGridView">
        <div id="particleQuizBannerWrap" style="display:none; padding:4px 16px 0 16px;">
          <div class="item-row-card" style="cursor:pointer; border-color:var(--accent-green); background:var(--card-elevated);" onclick="openParticleQuiz()">
            <div style="display:flex; align-items:center; gap:12px;">
              <div style="width:40px; height:40px; border-radius:50%; background:rgba(0, 230, 118, 0.2); display:flex; align-items:center; justify-content:center; font-size:18px;">📝</div>
              <div>
                <div style="font-size:14.5px; font-weight:800; color:var(--text-main);">Particle Mastery Quiz • প্র্যাকটিস কুইজ</div>
                <div style="font-size:11.5px; color:var(--text-muted);">16 Official Questions with Explanations</div>
              </div>
            </div>
            <div style="color:var(--accent-green); font-size:18px;">➔</div>
          </div>
        </div>
        <div id="grammarBoxGrid" class="stacked-box-grid"></div>
      </div>

      <div class="items-list" id="grammarContainer" style="display:none; margin-top:10px;"></div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 4: KANJI                                -->
    <!-- ============================================== -->
    <div id="screenKanji" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Kanji</div>
        </div>
        <div style="display:flex; gap:6px;">
          <button class="topbar-btn" onclick="openFlashcards('KANJI')" title="Flashcards">🗂️</button>
          <button class="topbar-btn" onclick="openQuiz('KANJI')" title="Quiz">🎮</button>
        </div>
      </div>

      <div class="search-input-box">
        <input type="text" class="search-input" id="kanjiSearchInput" placeholder="Search Kanji, Onyomi/Kunyomi, or Bangla..." oninput="renderKanjiList()">
      </div>

      <div class="chip-scroll-row" id="kanjiRangeChips">
        <button class="chip-btn active" onclick="filterKanjiRange('All', this)">All (110)</button>
        <button class="chip-btn" onclick="filterKanjiRange('#1-25', this)">#1-25</button>
        <button class="chip-btn" onclick="filterKanjiRange('#26-50', this)">#26-50</button>
        <button class="chip-btn" onclick="filterKanjiRange('#51-75', this)">#51-75</button>
        <button class="chip-btn" onclick="filterKanjiRange('#76-100', this)">#76-100</button>
        <button class="chip-btn" onclick="filterKanjiRange('#101-110', this)">#101-110</button>
      </div>

      <div class="items-list" id="kanjiItemsList"></div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 5: LISTENING                            -->
    <!-- ============================================== -->
    <div id="screenListening" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Listening</div>
          <div class="topbar-subtitle">87 Original CDs (CD 87 to 1)</div>
        </div>
        <button class="topbar-btn" id="listeningSortBtn" onclick="toggleCdSort()" title="Sort Order">▼</button>
      </div>

      <div class="search-input-box">
        <input type="text" class="search-input" id="listeningSearchInput" placeholder="Search CD number (e.g. 87), lesson, or topic..." oninput="renderCdList()">
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; padding:0 16px 8px 16px;">
        <button class="chip-btn active" id="sortLabelBtn" onclick="toggleCdSort()">Descending (CD 87 → 1)</button>
        <span style="font-size:12px; font-weight:800; color:var(--text-muted);" id="cdCountLabel">87 CDs Total</span>
      </div>

      <div class="chip-scroll-row" id="cdLessonChips"></div>

      <div class="items-list" id="cdTracksList"></div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 6: INTERVIEW                            -->
    <!-- ============================================== -->
    <div id="screenInterview" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Interview</div>
          <div class="topbar-subtitle">194 Official QA with Native Pronunciation</div>
        </div>
        <div style="width:40px;"></div>
      </div>

      <div class="search-input-box">
        <input type="text" class="search-input" id="interviewSearchInput" placeholder="Search interview questions in Bangla or Japanese..." oninput="renderInterviewList()">
      </div>

      <div class="chip-scroll-row" id="interviewCatChips">
        <button class="chip-btn active" onclick="filterInterviewCat('All', this)">All</button>
        <button class="chip-btn" onclick="filterInterviewCat('Myself', this)">Myself</button>
        <button class="chip-btn" onclick="filterInterviewCat('Family', this)">Family</button>
        <button class="chip-btn" onclick="filterInterviewCat('Japan', this)">Japan</button>
        <button class="chip-btn" onclick="filterInterviewCat('General Knowledge', this)">General Knowledge</button>
      </div>

      <div class="items-list" id="interviewItemsList"></div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 7: COUNTING UNITS                       -->
    <!-- ============================================== -->
    <div id="screenCounting" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Counting Units</div>
          <div class="topbar-subtitle">18 Essential JLPT N5 Counters</div>
        </div>
        <div style="width:40px;"></div>
      </div>

      <div class="items-list" id="countingItemsList" style="margin-top:12px;"></div>
    </div>


    <!-- ============================================== -->
    <!-- SCREEN 8: BOOKMARKS                            -->
    <!-- ============================================== -->
    <div id="screenBookmarks" class="screen">
      <div class="app-topbar">
        <button class="topbar-btn" onclick="openScreen('screenDashboard')">←</button>
        <div class="topbar-title-box">
          <div class="topbar-title">Bookmarks</div>
          <div class="topbar-subtitle">Saved Items for Quick Review</div>
        </div>
        <div style="width:40px;"></div>
      </div>

      <div class="items-list" id="bookmarksItemsList" style="margin-top:12px;"></div>
    </div>


    <!-- ============================================== -->
    <!-- 3D FLASHCARD MODAL                             -->
    <!-- ============================================== -->
    <div id="flashcardModal" class="flashcard-modal-overlay">
      <div class="modal-aura-bg">
        <div class="aura-layer aura-purple"></div>
        <div class="aura-layer aura-orange"></div>
        <div class="aura-layer aura-green"></div>
      </div>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <button class="topbar-btn" onclick="closeFlashcards()">✕</button>
        <div style="text-align:center;">
          <div style="font-size:15px; font-weight:800; color:var(--text-main);" id="fcTitle">Flashcards</div>
          <div style="font-size:11px; color:var(--accent-yellow);" id="fcProgress">Card 1 of 20</div>
        </div>
        <div style="display:flex; gap:6px;">
          <button class="topbar-btn" onclick="toggleFcReverse()" title="Reverse Mode">🔄</button>
          <button class="topbar-btn" onclick="shuffleFlashcards()" title="Shuffle">🔀</button>
        </div>
      </div>

      <div class="fc-card-container">
        <div class="fc-3d-card" id="fcCard" onclick="flipFlashcard()">
          <!-- Front Face -->
          <div class="fc-face front">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <span class="badge-pill" id="fcFrontBadge">LESSON</span>
              <span style="font-size:10px; font-weight:800; color:var(--text-muted);">QUESTION</span>
            </div>
            <div>
              <div class="fc-main-jp" id="fcFrontJp">わたし</div>
              <div class="fc-romaji" id="fcFrontRomaji">watashi</div>
            </div>
            <div style="display:flex; justify-content:center;">
              <button class="speaker-btn" style="width:52px; height:52px; font-size:22px;" onclick="event.stopPropagation(); playCurrentFcAudio();">🔊</button>
            </div>
            <div class="fc-bottom-hint">কার্ডে চাপ দিয়ে উত্তর দেখুন (Tap to flip) ↺</div>
          </div>

          <!-- Back Face -->
          <div class="fc-face back">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <span class="badge-pill" style="background:rgba(0, 230, 118, 0.15); color:var(--accent-green); border-color:var(--accent-green);" id="fcBackBadge">ANSWER</span>
              <span style="font-size:10px; font-weight:800; color:var(--accent-green);">MEANING</span>
            </div>
            <div>
              <div class="fc-main-bn" id="fcBackBn">আমি</div>
              <div style="font-size:14px; color:var(--text-muted); margin-top:4px;" id="fcBackEn">I, myself</div>
              <div style="display:inline-block; padding:4px 10px; background:var(--card-bg); border-radius:8px; font-size:14px; font-weight:700; color:var(--text-main); margin-top:10px;" id="fcBackOriginal">わたし (watashi)</div>
            </div>
            <div style="display:flex; justify-content:center;">
              <button class="speaker-btn" style="width:52px; height:52px; font-size:22px; color:var(--accent-green);" onclick="event.stopPropagation(); playCurrentFcAudio();">🔊</button>
            </div>
            <div class="fc-bottom-hint" style="color:var(--accent-green); border-color:var(--accent-green);">প্রশ্ন দেখতে আবার চাপুন (Flip back) ↺</div>
          </div>
        </div>
      </div>

      <!-- Bottom Controls -->
      <div>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
          <button class="chip-btn" id="fcAutoVoiceBtn" onclick="toggleFcAutoVoice()">অটো-ভয়েস ON 🔊</button>
          <div style="display:flex; gap:8px;">
            <button class="chip-btn" style="color:var(--primary); border-color:var(--primary);" onclick="fcMarkReview()">শিখছি ✕</button>
            <button class="chip-btn" style="color:var(--accent-green); border-color:var(--accent-green);" onclick="fcMarkMastered()">পারি ✓</button>
          </div>
        </div>

        <div class="fc-bottom-controls">
          <button class="topbar-btn" style="width:52px; height:52px;" onclick="prevFlashcard()">←</button>
          <button class="chip-btn" style="padding:12px 24px; font-size:14px; font-weight:800;" onclick="flipFlashcard()">ফ্লিপ করুন (Flip ↺)</button>
          <button class="topbar-btn" style="width:52px; height:52px; background:var(--primary); color:#fff;" onclick="nextFlashcard()">→</button>
        </div>
      </div>
    </div>


    <!-- ============================================== -->
    <!-- INTERACTIVE QUIZ MODAL                         -->
    <!-- ============================================== -->
    <div id="quizModal" class="quiz-modal-overlay">
      <div class="modal-aura-bg">
        <div class="aura-layer aura-purple"></div>
        <div class="aura-layer aura-orange"></div>
        <div class="aura-layer aura-green"></div>
      </div>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <button class="topbar-btn" onclick="closeQuiz()">✕</button>
        <div style="text-align:center;">
          <div style="font-size:15px; font-weight:800; color:var(--text-main);" id="quizTitle">Interactive Quiz</div>
          <div style="font-size:11px; color:var(--accent-yellow);" id="quizProgress">Question 1 of 10</div>
        </div>
        <div style="font-size:14px; font-weight:800; color:var(--accent-green);" id="quizScoreText">Score: 0</div>
      </div>

      <div class="quiz-question-card">
        <div style="font-size:12px; color:var(--text-muted); font-weight:700; margin-bottom:8px;" id="quizCategoryBadge">VOCABULARY QUIZ</div>
        <div class="quiz-prompt-text" id="quizPrompt">わたし</div>
        <div style="margin-top:14px;">
          <button class="speaker-btn" style="margin:0 auto;" onclick="speakQuizPrompt()">🔊</button>
        </div>
      </div>

      <div class="quiz-options-grid" id="quizOptionsContainer"></div>

      <div style="text-align:center; font-size:12px; color:var(--text-muted);">সঠিক উত্তরে ট্যাপ করুন</div>
    </div>

  </div>

  <!-- Audio Element for MP3 CD & Fallback playback -->
  <audio id="globalAudioPlayer" preload="none"></audio>

  <script>
    // Embedded Data Sets
    const ALL_ITEMS = ''' + json.dumps(all_items, ensure_ascii=False) + ''';
    const CD_TRACKS = ''' + json.dumps(cd_tracks, ensure_ascii=False) + ''';
    const GRAMMAR_LESSONS = ''' + json.dumps(grammar_lessons, ensure_ascii=False) + ''';
    const HIRAGANA_ITEMS = ''' + json.dumps(hiragana_items, ensure_ascii=False) + ''';
    const KATAKANA_ITEMS = ''' + json.dumps(katakana_items, ensure_ascii=False) + ''';
    const INTERVIEW_ITEMS = ''' + json.dumps(interview_items, ensure_ascii=False) + ''';
    const PARTICLE_RULES = ''' + json.dumps(particle_rules, ensure_ascii=False) + ''';
    const PARTICLE_QUESTIONS = ''' + json.dumps(particle_questions, ensure_ascii=False) + ''';
    const CD_ASSET_MAP = ''' + json.dumps(cd_asset_map, ensure_ascii=False) + ''';

    // Application State
    let currentKanaType = 'HIRAGANA';
    let currentPracticeKanaIndex = 0;
    let selectedVocabLesson = 1;
    let currentGrammarTab = 'LESSONS';
    let selectedKanjiRange = 'All';
    let isCdDescending = true;
    let selectedCdLesson = null;
    let playingCdNumber = null;
    let cdPlaybackSpeed = 1.0;
    let selectedInterviewCat = 'All';

    // Flashcard State
    let fcList = [];
    let fcIndex = 0;
    let fcIsReverse = false;
    let fcAutoVoice = true;

    // Quiz State
    let quizList = [];
    let quizIndex = 0;
    let quizScore = 0;

    // Bookmarks (LocalStorage)
    let bookmarks = JSON.parse(localStorage.getItem('jd_bookmarks') || '[]');

    // Audio Player instance
    const audioPlayer = document.getElementById('globalAudioPlayer');

    // -------------------------------------------------------------
    // Audio Pronunciation Engine (Native Speech + Google Fallback)
    // -------------------------------------------------------------
    function playAudio(text, btnElement) {
      if (!text) return;
      const clean = text.replace(/[\\[\\]~～()（）/／。?？]/g, '').trim();
      if (!clean) return;

      if (btnElement) {
        btnElement.classList.add('playing');
        setTimeout(() => btnElement.classList.remove('playing'), 1200);
      }

      let spoken = false;
      if ('speechSynthesis' in window) {
        try {
          window.speechSynthesis.cancel();
          const u = new SpeechSynthesisUtterance(clean);
          u.lang = 'ja-JP';
          u.rate = 0.88;
          u.onerror = function() {
            playOnlineTtsFallback(clean);
          };
          window.speechSynthesis.speak(u);
          spoken = true;
        } catch (e) {
          spoken = false;
        }
      }

      if (!spoken) {
        playOnlineTtsFallback(clean);
      }
    }

    function playOnlineTtsFallback(text) {
      try {
        const url = 'https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ja&q=' + encodeURIComponent(text);
        const a = new Audio(url);
        a.play().catch(function(err) {
          console.log('Online audio error:', err);
        });
      } catch (err) {
        console.log('TTS fallback error:', err);
      }
    }

    // -------------------------------------------------------------
    // CD Audio Player Engine (All 87 CDs with multi-tier source)
    // -------------------------------------------------------------
    function playCdTrack(cdNumber) {
      const card = document.getElementById(`cdCard_${cdNumber}`);
      if (playingCdNumber === cdNumber && !audioPlayer.paused) {
        audioPlayer.pause();
        if (card) card.classList.remove('playing');
        return;
      }

      // Stop previous
      if (playingCdNumber) {
        const prevCard = document.getElementById(`cdCard_${playingCdNumber}`);
        if (prevCard) {
          prevCard.classList.remove('playing', 'active-player');
        }
      }

      playingCdNumber = cdNumber;
      if (card) {
        card.classList.add('playing', 'active-player');
      }

      // Build fallback URLs
      const numStr = String(cdNumber).padStart(2, '0');
      const candidateUrls = [
        `audio/cd_${numStr}.mp3`,
        `./audio/cd_${numStr}.mp3`,
        `public/audio/cd_${numStr}.mp3`,
        `cd_${numStr}.mp3`,
        CD_ASSET_MAP[cdNumber] || '',
        `https://ais-pre-y4tlq4crctsknlvowh5gpl-763162333956.asia-northeast1.run.app/audio/cd_${numStr}.mp3`
      ].filter(Boolean);

      let urlIndex = 0;
      function tryNextSource() {
        if (urlIndex < candidateUrls.length) {
          audioPlayer.src = candidateUrls[urlIndex];
          urlIndex++;
          audioPlayer.playbackRate = cdPlaybackSpeed;
          audioPlayer.play().catch(() => {
            tryNextSource();
          });
        }
      }

      tryNextSource();
    }

    // Audio Event Handlers
    audioPlayer.ontimeupdate = function() {
      if (!playingCdNumber) return;
      const slider = document.getElementById(`cdSeek_${playingCdNumber}`);
      const currTime = document.getElementById(`cdCurrTime_${playingCdNumber}`);
      const totalTime = document.getElementById(`cdTotalTime_${playingCdNumber}`);
      if (slider && audioPlayer.duration) {
        slider.value = (audioPlayer.currentTime / audioPlayer.duration) * 100;
        if (currTime) currTime.textContent = formatAudioTime(audioPlayer.currentTime);
        if (totalTime) totalTime.textContent = formatAudioTime(audioPlayer.duration);
      }
    };

    audioPlayer.onended = function() {
      if (playingCdNumber) {
        const card = document.getElementById(`cdCard_${playingCdNumber}`);
        if (card) card.classList.remove('playing');
      }
    };

    function onSeekCd(cdNumber, slider) {
      if (playingCdNumber === cdNumber && audioPlayer.duration) {
        audioPlayer.currentTime = (slider.value / 100) * audioPlayer.duration;
      }
    }

    function cdRewind(seconds) {
      if (audioPlayer.currentTime) {
        audioPlayer.currentTime = Math.max(0, audioPlayer.currentTime - seconds);
      }
    }

    function cdForward(seconds) {
      if (audioPlayer.currentTime && audioPlayer.duration) {
        audioPlayer.currentTime = Math.min(audioPlayer.duration, audioPlayer.currentTime + seconds);
      }
    }

    function cycleCdSpeed(cdNumber) {
      cdPlaybackSpeed = (cdPlaybackSpeed === 1.0) ? 1.2 : (cdPlaybackSpeed === 1.2) ? 0.8 : 1.0;
      audioPlayer.playbackRate = cdPlaybackSpeed;
      const btn = document.getElementById(`cdSpeedBtn_${cdNumber}`);
      if (btn) btn.textContent = `${cdPlaybackSpeed}x`;
    }

    function toggleCdScript(cdNumber) {
      const box = document.getElementById(`cdScript_${cdNumber}`);
      const toggle = document.getElementById(`cdScriptToggle_${cdNumber}`);
      if (box) {
        box.classList.toggle('expanded');
        if (toggle) {
          toggle.textContent = box.classList.contains('expanded') ? 'স্ক্রিপ্ট লুকান (Hide Script) ▴' : 'স্ক্রিপ্ট ও বাংলা অর্থ দেখুন (View Script) ▾';
        }
      }
    }

    function formatAudioTime(sec) {
      if (isNaN(sec)) return '00:00';
      const m = Math.floor(sec / 60);
      const s = Math.floor(sec % 60);
      return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    }

    // -------------------------------------------------------------
    // Screen Navigation
    // -------------------------------------------------------------
    function openScreen(screenId) {
      document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
      const target = document.getElementById(screenId);
      if (target) {
        target.classList.add('active');
        window.scrollTo(0, 0);
      }

      // Screen specific initializers
      updateAmbientAura(screenId !== 'screenDashboard');
      if (screenId === 'screenKana') renderKanaGrid();
      if (screenId === 'screenVocab') { activeVocabLesson = null; showVocabMasterQuiz = false; renderVocabView(); }
      if (screenId === 'screenGrammar') { activeGrammarLesson = null; activeParticleRule = null; renderGrammar(); }
      if (screenId === 'screenKanji') renderKanjiList();
      if (screenId === 'screenListening') { renderCdChips(); renderCdList(); }
      if (screenId === 'screenInterview') renderInterviewList();
      if (screenId === 'screenCounting') renderCountingList();
      if (screenId === 'screenBookmarks') renderBookmarksList();
    }

    function updateAmbientAura(enabled) {
      const el = document.getElementById('ambientAuraBg');
      if (!el) return;
      el.classList.toggle('active', Boolean(enabled));
    }

    // Theme Toggle
    function toggleTheme() {
      document.body.classList.toggle('light-theme');
      localStorage.setItem('jd_theme', document.body.classList.contains('light-theme') ? 'light' : 'dark');
    }
    if (localStorage.getItem('jd_theme') === 'light') {
      document.body.classList.add('light-theme');
    }

    // -------------------------------------------------------------
    // 1. KANA (হিরাগানা কাতাকানা) SCREEN
    // -------------------------------------------------------------
    function switchKanaTab(tab) {
      currentKanaType = tab;
      document.getElementById('tabKanaHira').classList.toggle('active', tab === 'HIRAGANA');
      document.getElementById('tabKanaKata').classList.toggle('active', tab === 'KATAKANA');
      document.getElementById('tabKanaCanvas').classList.toggle('active', tab === 'CANVAS');

      const grid = document.getElementById('kanaGridContainer');
      const canvasBox = document.getElementById('kanaCanvasContainer');

      if (tab === 'CANVAS') {
        grid.style.display = 'none';
        canvasBox.style.display = 'flex';
        initDrawingCanvas();
        updatePracticeKana();
      } else {
        grid.style.display = 'grid';
        canvasBox.style.display = 'none';
        renderKanaGrid();
      }
    }

    function renderKanaGrid() {
      const container = document.getElementById('kanaGridContainer');
      const list = (currentKanaType === 'KATAKANA') ? KATAKANA_ITEMS : HIRAGANA_ITEMS;
      container.innerHTML = list.map(it => `
        <div class="item-row-card" style="flex-direction:column; padding:12px; text-align:center; cursor:pointer;" onclick="playAudio('${it.japanese}', this)">
          <div style="font-size:28px; font-weight:900; color:var(--text-main); font-family:'Noto Sans JP';">${it.japanese}</div>
          <div style="font-size:12px; font-weight:700; color:var(--accent-yellow);">${it.romaji}</div>
          <div style="font-size:11px; color:var(--accent-green);">${it.bangla}</div>
        </div>
      `).join('');
    }

    // Handwriting Canvas
    let canvas, ctx, isDrawing = false;
    function initDrawingCanvas() {
      canvas = document.getElementById('drawingCanvas');
      if (!canvas) return;
      ctx = canvas.getContext('2d');
      canvas.width = canvas.parentElement.clientWidth;
      canvas.height = canvas.parentElement.clientHeight;

      function startDraw(e) {
        isDrawing = true;
        ctx.beginPath();
        const pos = getPos(e);
        ctx.moveTo(pos.x, pos.y);
      }
      function draw(e) {
        if (!isDrawing) return;
        const pos = getPos(e);
        ctx.lineTo(pos.x, pos.y);
        ctx.strokeStyle = '#00E676';
        ctx.lineWidth = 14;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';
        ctx.stroke();
      }
      function stopDraw() { isDrawing = false; }
      function getPos(e) {
        const rect = canvas.getBoundingClientRect();
        const clientX = e.touches ? e.touches[0].clientX : e.clientX;
        const clientY = e.touches ? e.touches[0].clientY : e.clientY;
        return { x: clientX - rect.left, y: clientY - rect.top };
      }

      canvas.onmousedown = startDraw;
      canvas.onmousemove = draw;
      canvas.onmouseup = stopDraw;
      canvas.ontouchstart = startDraw;
      canvas.ontouchmove = draw;
      canvas.ontouchend = stopDraw;
    }

    function clearCanvas() {
      if (ctx && canvas) ctx.clearRect(0, 0, canvas.width, canvas.height);
    }

    function updatePracticeKana() {
      const item = HIRAGANA_ITEMS[currentPracticeKanaIndex % HIRAGANA_ITEMS.length];
      document.getElementById('currentTracingLabel').textContent = `${item.japanese} (${item.romaji}) - ${item.bangla}`;
      document.getElementById('canvasWatermarkChar').textContent = item.japanese;
      clearCanvas();
    }

    function nextPracticeKana() {
      currentPracticeKanaIndex++;
      updatePracticeKana();
    }

    function speakCurrentKana() {
      const item = HIRAGANA_ITEMS[currentPracticeKanaIndex % HIRAGANA_ITEMS.length];
      playAudio(item.japanese);
    }

    // -------------------------------------------------------------
    // 2. VOCABULARY SCREEN (Stacked Box Grid + Lesson Detail)
    // -------------------------------------------------------------
    const BOX_PALETTE = [
      { front: '#00E5FF', back: '#FF2E55' },
      { front: '#9D7BFF', back: '#FFA114' },
      { front: '#00E676', back: '#00E5FF' },
      { front: '#FF699A', back: '#00E676' },
      { front: '#FFA114', back: '#9D7BFF' },
      { front: '#FF2E55', back: '#FFA114' }
    ];
    let activeVocabLesson = null;
    let showVocabMasterQuiz = false;
    let activeGrammarLesson = null;
    let activeParticleRule = null;

    function buildStackedBoxHtml({ title, badge, iconText, onclickCode }) {
      const len = String(iconText).length;
      const iconFontSize = len > 3 ? '16px' : (len > 2 ? '19px' : '22px');
      return `
        <div class="stacked-box-wrap" onclick="${onclickCode}">
          <div class="stacked-box-front">
            <div class="stacked-box-icon" style="font-size:${iconFontSize};">${iconText}</div>
            <div class="stacked-box-title">${title}</div>
            <div class="stacked-box-badge">${badge}</div>
          </div>
        </div>
      `;
    }

    function backFromVocab() {
      if (activeVocabLesson !== null || showVocabMasterQuiz) {
        activeVocabLesson = null;
        showVocabMasterQuiz = false;
        const inp = document.getElementById('vocabSearchInput');
        if (inp) inp.value = '';
        renderVocabView();
      } else {
        openScreen('screenDashboard');
      }
    }

    function openVocabLesson(lesson) {
      activeVocabLesson = lesson;
      selectedVocabLesson = lesson;
      showVocabMasterQuiz = false;
      const inp = document.getElementById('vocabSearchInput');
      if (inp) inp.value = '';
      renderVocabView();
      window.scrollTo(0, 0);
    }

    function openVocabMasterQuiz() {
      activeVocabLesson = null;
      showVocabMasterQuiz = true;
      renderVocabView();
      window.scrollTo(0, 0);
    }

    function renderVocabView() {
      const gridEl = document.getElementById('vocabGridContainer');
      const detailEl = document.getElementById('vocabDetailContainer');
      const placeholderEl = document.getElementById('vocabMasterQuizPlaceholder');
      const titleEl = document.getElementById('vocabTopTitle');
      const subEl = document.getElementById('vocabTopSubtitle');

      if (showVocabMasterQuiz) {
        updateAmbientAura(true);
        gridEl.style.display = 'none';
        detailEl.style.display = 'none';
        placeholderEl.style.display = 'block';
        titleEl.textContent = 'Vocabulary Quiz';
        subEl.style.display = 'none';
        return;
      }

      if (activeVocabLesson !== null) {
        updateAmbientAura(true);
        gridEl.style.display = 'none';
        placeholderEl.style.display = 'none';
        detailEl.style.display = 'flex';
        const fullList = ALL_ITEMS.filter(it => it.category === 'VOCABULARY' && it.lesson === activeVocabLesson);
        titleEl.textContent = `Lesson ${activeVocabLesson}`;
        subEl.textContent = `${fullList.length} Words`;
        subEl.style.display = 'block';
        document.getElementById('vocabFlashcardBtnText').textContent = `Flashcards (${fullList.length})`;
        document.getElementById('vocabQuizBtnText').textContent = `Take Quiz (${fullList.length})`;
        renderVocabList();
        return;
      }

      updateAmbientAura(true);
      detailEl.style.display = 'none';
      placeholderEl.style.display = 'none';
      gridEl.style.display = 'grid';
      titleEl.textContent = 'Vocabulary';
      subEl.style.display = 'none';

      let html = '';
      for (let l = 1; l <= 25; l++) {
        const count = ALL_ITEMS.filter(it => it.category === 'VOCABULARY' && it.lesson === l).length;
        html += buildStackedBoxHtml({
          title: `Lesson ${l}`,
          badge: `${count} Words`,
          iconText: `L${l}`,
          paletteIdx: l - 1,
          tiltDir: (l - 1) % 2 === 0 ? 1 : -1,
          onclickCode: `openVocabLesson(${l})`,
          isDimmed: false
        });
      }
      html += buildStackedBoxHtml({
        title: 'Vocabulary Quiz',
        badge: '25 Lessons',
        iconText: '📝',
        paletteIdx: 25,
        tiltDir: -1,
        onclickCode: 'openVocabMasterQuiz()',
        isDimmed: true
      });
      gridEl.innerHTML = html;
    }

    function renderVocabList() {
      const query = (document.getElementById('vocabSearchInput')?.value || '').toLowerCase().trim();
      const container = document.getElementById('vocabItemsList');
      let list = ALL_ITEMS.filter(it => it.category === 'VOCABULARY' && it.lesson === selectedVocabLesson);

      if (query) {
        list = list.filter(it =>
          it.japanese.toLowerCase().includes(query) ||
          it.romaji.toLowerCase().includes(query) ||
          it.bangla.toLowerCase().includes(query)
        );
      }

      const countEl = document.getElementById('vocabLessonCountLabel');
      if (countEl) countEl.textContent = `${list.length} WORDS IN LESSON ${selectedVocabLesson}`;

      container.innerHTML = list.map(it => {
        const isBm = bookmarks.some(b => b.id === it.id);
        return `
          <div class="item-row-card">
            <div class="item-main-content">
              <div class="item-jp-text">${it.japanese}</div>
              <div class="item-romaji-text">${it.romaji}</div>
              <div class="item-bn-text">${it.bangla}</div>
              ${it.english ? `<div class="item-en-text">${it.english}</div>` : ''}
            </div>
            <div class="item-actions">
              <button class="speaker-btn" onclick="playAudio(decodeURIComponent('${encodeURIComponent(it.audioPhrase || it.japanese)}'), this)">🔊</button>
              <button class="star-btn ${isBm ? 'active' : ''}" onclick="toggleBookmark('${it.id}', this)">★</button>
            </div>
          </div>
        `;
      }).join('');
    }

    // -------------------------------------------------------------
    // 3. GRAMMAR & PARTICLES SCREEN (Stacked Box Grid + Detail View)
    // -------------------------------------------------------------
    function switchGrammarTab(tab) {
      currentGrammarTab = tab;
      activeGrammarLesson = null;
      activeParticleRule = null;
      document.getElementById('tabGrammarLessons').classList.toggle('active', tab === 'LESSONS');
      document.getElementById('tabGrammarParticles').classList.toggle('active', tab === 'PARTICLES');
      renderGrammar();
    }

    function backFromGrammar() {
      if (activeGrammarLesson !== null || activeParticleRule !== null) {
        activeGrammarLesson = null;
        activeParticleRule = null;
        renderGrammar();
      } else {
        openScreen('screenDashboard');
      }
    }

    function openGrammarLesson(lessonNum) {
      activeGrammarLesson = GRAMMAR_LESSONS.find(l => l.lesson === lessonNum) || null;
      renderGrammar();
      window.scrollTo(0, 0);
    }

    function openParticleDetail(idx) {
      activeParticleRule = PARTICLE_RULES[idx] || null;
      renderGrammar();
      window.scrollTo(0, 0);
    }

    function renderGrammar() {
      const tabRow = document.getElementById('grammarTabRow');
      const gridView = document.getElementById('grammarGridView');
      const boxGrid = document.getElementById('grammarBoxGrid');
      const bannerWrap = document.getElementById('particleQuizBannerWrap');
      const detailList = document.getElementById('grammarContainer');
      const titleEl = document.getElementById('grammarTopTitle');
      const subEl = document.getElementById('grammarTopSubtitle');
      const topQuizBtn = document.getElementById('grammarTopQuizBtn');

      if (activeGrammarLesson !== null) {
        updateAmbientAura(true);
        const l = activeGrammarLesson;
        tabRow.style.display = 'none';
        gridView.style.display = 'none';
        detailList.style.display = 'flex';
        topQuizBtn.style.visibility = 'hidden';
        titleEl.textContent = `Lesson ${l.lesson}`;
        subEl.textContent = `${l.rules.length} Rules • Short Grammar`;
        subEl.style.display = 'block';

        detailList.innerHTML = l.rules.map((r, idx) => `
          <div class="item-row-card" style="flex-direction:column; align-items:flex-start; gap:8px;">
            <div style="font-size:14.5px; font-weight:800; color:var(--accent-green);">Rule ${idx + 1}: ${r.pattern}</div>
            ${r.formula && r.formula.trim() ? `<div style="font-size:12px; font-weight:600; color:var(--accent-yellow);">সূত্র: ${r.formula}</div>` : ''}
            ${r.explanation && r.explanation.trim() ? `<div style="font-size:13px; color:var(--text-muted); line-height:1.55; white-space:pre-line;">${r.explanation}</div>` : ''}
            ${r.exampleJp && r.exampleJp.trim() ? `
              <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; width:100%; margin-top:4px; padding:10px 12px; border-radius:10px; background:var(--bg); border:1px solid var(--card-border);">
                <div style="flex:1;">
                  <div style="font-size:13.5px; font-weight:700; color:var(--text-main); white-space:pre-line; line-height:1.45;">${r.exampleJp}</div>
                  ${r.exampleRomaji && r.exampleRomaji.trim() ? `<div style="font-size:11.5px; color:var(--accent-yellow); margin-top:2px; white-space:pre-line;">${r.exampleRomaji}</div>` : ''}
                  ${r.exampleBangla && r.exampleBangla.trim() ? `<div style="font-size:12px; color:var(--accent-green); margin-top:2px; white-space:pre-line;">${r.exampleBangla}</div>` : ''}
                </div>
                <button class="speaker-btn" style="width:36px; height:36px; font-size:15px; flex-shrink:0;" onclick="playAudio(decodeURIComponent('${encodeURIComponent(r.exampleJp)}'), this)">🔊</button>
              </div>
            ` : ''}
          </div>
        `).join('');
        return;
      }

      if (activeParticleRule !== null) {
        updateAmbientAura(true);
        const p = activeParticleRule;
        const pts = (p.points && p.points.length) ? p.points : [{ usage: p.usage, exampleJp: p.exampleJp }];
        tabRow.style.display = 'none';
        gridView.style.display = 'none';
        detailList.style.display = 'flex';
        topQuizBtn.style.visibility = 'hidden';
        titleEl.textContent = `${p.particle} (${p.romaji})`;
        subEl.textContent = p.title;
        subEl.style.display = 'block';

        detailList.innerHTML = pts.map((pt, idx) => `
          <div class="item-row-card" style="flex-direction:column; align-items:flex-start; gap:8px;">
            <span style="display:inline-block; font-size:11px; font-weight:800; color:var(--accent-cyan); background:rgba(0, 229, 255, 0.14); padding:2px 9px; border-radius:6px;">Point ${idx + 1}</span>
            ${pt.usage && pt.usage.trim() ? `<div style="font-size:13.5px; color:var(--text-muted); line-height:1.55; white-space:pre-line;">${pt.usage}</div>` : ''}
            ${pt.exampleJp && pt.exampleJp.trim() ? `
              <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; width:100%; background:var(--bg); border:1px solid var(--card-border); border-radius:10px; padding:10px 12px; margin-top:2px;">
                <div style="font-size:14px; font-weight:700; color:var(--text-main); line-height:1.45; white-space:pre-line; flex:1;">${pt.exampleJp}</div>
                <button class="speaker-btn" style="width:36px; height:36px; font-size:15px; flex-shrink:0;" onclick="playAudio(decodeURIComponent('${encodeURIComponent(pt.exampleJp)}'), this)">🔊</button>
              </div>
            ` : ''}
          </div>
        `).join('');
        return;
      }

      // Main Hub Box Grid View
      updateAmbientAura(true);
      tabRow.style.display = 'flex';
      gridView.style.display = 'block';
      detailList.style.display = 'none';
      titleEl.textContent = 'Grammar';
      subEl.style.display = 'none';

      if (currentGrammarTab === 'LESSONS') {
        topQuizBtn.style.visibility = 'hidden';
        bannerWrap.style.display = 'none';
        boxGrid.innerHTML = GRAMMAR_LESSONS.map((l, idx) => buildStackedBoxHtml({
          title: `Lesson ${l.lesson}`,
          badge: `${l.rules.length} Rules`,
          iconText: `L${l.lesson}`,
          paletteIdx: idx,
          tiltDir: idx % 2 === 0 ? 1 : -1,
          onclickCode: `openGrammarLesson(${l.lesson})`,
          isDimmed: false
        })).join('');
      } else {
        topQuizBtn.style.visibility = 'visible';
        bannerWrap.style.display = 'block';
        boxGrid.innerHTML = PARTICLE_RULES.map((p, idx) => {
          const ptsCount = (p.points && p.points.length) ? p.points.length : 1;
          return buildStackedBoxHtml({
            title: p.romaji,
            badge: `${ptsCount} ${ptsCount === 1 ? 'Point' : 'Points'}`,
            iconText: p.particle,
            paletteIdx: idx,
            tiltDir: idx % 2 === 0 ? 1 : -1,
            onclickCode: `openParticleDetail(${idx})`,
            isDimmed: false
          });
        }).join('');
      }
    }

    // -------------------------------------------------------------
    // 4. KANJI SCREEN
    // -------------------------------------------------------------
    function filterKanjiRange(range, btn) {
      selectedKanjiRange = range;
      document.querySelectorAll('#kanjiRangeChips .chip-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderKanjiList();
    }

    function renderKanjiList() {
      const query = (document.getElementById('kanjiSearchInput')?.value || '').toLowerCase().trim();
      const container = document.getElementById('kanjiItemsList');
      let kanji = ALL_ITEMS.filter(it => it.category === 'KANJI');

      if (selectedKanjiRange === '#1-25') kanji = kanji.slice(0, 25);
      else if (selectedKanjiRange === '#26-50') kanji = kanji.slice(25, 50);
      else if (selectedKanjiRange === '#51-75') kanji = kanji.slice(50, 75);
      else if (selectedKanjiRange === '#76-100') kanji = kanji.slice(75, 100);
      else if (selectedKanjiRange === '#101-110') kanji = kanji.slice(100);

      if (query) {
        kanji = kanji.filter(it => 
          it.japanese.toLowerCase().includes(query) ||
          it.romaji.toLowerCase().includes(query) ||
          it.bangla.toLowerCase().includes(query)
        );
      }

      container.innerHTML = kanji.map(it => {
        const isBm = bookmarks.some(b => b.id === it.id);
        return `
          <div class="item-row-card">
            <div style="display:flex; align-items:center; gap:14px;">
              <div style="width:48px; height:48px; border-radius:12px; background:rgba(255, 161, 20, 0.15); display:flex; align-items:center; justify-content:center; font-size:26px; font-weight:900; color:var(--accent-yellow); font-family:'Noto Sans JP';">${it.japanese}</div>
              <div>
                <div style="font-size:13px; font-weight:700; color:var(--text-muted);">${it.romaji}</div>
                <div style="font-size:15px; font-weight:800; color:var(--accent-green);">${it.bangla}</div>
                ${it.english ? `<div style="font-size:11px; color:var(--text-muted);">${it.english}</div>` : ''}
              </div>
            </div>
            <div class="item-actions">
              <button class="speaker-btn" onclick="playAudio('${it.audioPhrase || it.japanese}', this)">🔊</button>
              <button class="star-btn ${isBm ? 'active' : ''}" onclick="toggleBookmark('${it.id}', this)">★</button>
            </div>
          </div>
        `;
      }).join('');
    }

    // -------------------------------------------------------------
    // 5. LISTENING (৮৭টি সিডি) SCREEN
    // -------------------------------------------------------------
    function toggleCdSort() {
      isCdDescending = !isCdDescending;
      document.getElementById('sortLabelBtn').textContent = isCdDescending ? 'ডিসেন্ডিং (CD 87 → 1)' : 'অ্যাসেন্ডিং (CD 1 → 87)';
      document.getElementById('listeningSortBtn').textContent = isCdDescending ? '▼' : '▲';
      renderCdList();
    }

    function renderCdChips() {
      const container = document.getElementById('cdLessonChips');
      let html = `<button class="chip-btn ${selectedCdLesson === null ? 'active' : ''}" onclick="selectCdLesson(null, this)">All CDs (সকল)</button>`;
      for (let l = 1; l <= 25; l++) {
        html += `<button class="chip-btn ${selectedCdLesson === l ? 'active' : ''}" onclick="selectCdLesson(${l}, this)">L${l}</button>`;
      }
      container.innerHTML = html;
    }

    function selectCdLesson(lesson, btn) {
      selectedCdLesson = lesson;
      document.querySelectorAll('#cdLessonChips .chip-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderCdList();
    }

    function renderCdList() {
      const query = (document.getElementById('listeningSearchInput')?.value || '').toLowerCase().trim();
      const container = document.getElementById('cdTracksList');

      let list = [...CD_TRACKS];
      if (selectedCdLesson) {
        list = list.filter(it => it.lessonNumber === selectedCdLesson);
      }
      if (query) {
        list = list.filter(it => 
          it.title.toLowerCase().includes(query) ||
          it.subtitle.toLowerCase().includes(query) ||
          String(it.cdNumber).includes(query) ||
          it.lesson.toLowerCase().includes(query)
        );
      }

      list.sort((a, b) => isCdDescending ? b.cdNumber - a.cdNumber : a.cdNumber - b.cdNumber);
      document.getElementById('cdCountLabel').textContent = `মোট ${list.length}টি সিডি`;

      container.innerHTML = list.map(t => {
        const isCurrentPlaying = (playingCdNumber === t.cdNumber && !audioPlayer.paused);
        return `
          <div class="cd-card ${isCurrentPlaying ? 'playing active-player' : ''}" id="cdCard_${t.cdNumber}">
            <div class="cd-header-row">
              <div class="cd-disc-icon">💿</div>
              <div class="cd-info-col">
                <div class="cd-title">${t.title}</div>
                <div class="cd-subtitle">${t.subtitle}</div>
              </div>
              <button class="cd-play-btn" onclick="playCdTrack(${t.cdNumber})">${isCurrentPlaying ? '⏸' : '▶'}</button>
            </div>

            <div class="cd-player-controls">
              <input type="range" class="seek-slider" id="cdSeek_${t.cdNumber}" value="0" min="0" max="100" onchange="onSeekCd(${t.cdNumber}, this)">
              <div class="time-row">
                <span id="cdCurrTime_${t.cdNumber}">00:00</span>
                <div class="sub-controls-row">
                  <button class="sub-ctrl-btn" onclick="cdRewind(5)">-5s</button>
                  <button class="sub-ctrl-btn" id="cdSpeedBtn_${t.cdNumber}" onclick="cycleCdSpeed(${t.cdNumber})">1x</button>
                  <button class="sub-ctrl-btn" onclick="cdForward(5)">+5s</button>
                </div>
                <span id="cdTotalTime_${t.cdNumber}">00:00</span>
              </div>
            </div>

            <div class="script-toggle-bar" onclick="toggleCdScript(${t.cdNumber})">
              <span>📄 <span id="cdScriptToggle_${t.cdNumber}">স্ক্রিপ্ট ও বাংলা অর্থ দেখুন (View Script) ▾</span></span>
            </div>

            <div class="script-content-box" id="cdScript_${t.cdNumber}">
              ${(t.script || []).map(line => `
                <div class="script-line-card">
                  <div>
                    <div class="script-speaker">${line.speaker}</div>
                    <div class="script-jp">${line.japanese}</div>
                    <div class="script-romaji">${line.romaji}</div>
                    <div class="script-bn">${line.bangla}</div>
                  </div>
                  <button class="speaker-btn" style="width:34px; height:34px; font-size:15px;" onclick="playAudio('${line.japanese}', this)">🔊</button>
                </div>
              `).join('')}
            </div>
          </div>
        `;
      }).join('');
    }

    // -------------------------------------------------------------
    // 6. INTERVIEW Q&A SCREEN
    // -------------------------------------------------------------
    function filterInterviewCat(cat, btn) {
      selectedInterviewCat = cat;
      document.querySelectorAll('#interviewCatChips .chip-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderInterviewList();
    }

    function renderInterviewList() {
      const query = (document.getElementById('interviewSearchInput')?.value || '').toLowerCase().trim();
      const container = document.getElementById('interviewItemsList');

      let list = [...INTERVIEW_ITEMS];
      if (selectedInterviewCat !== 'All') {
        list = list.filter(it => it.category === selectedInterviewCat);
      }
      if (query) {
        list = list.filter(it => 
          it.questionJp.toLowerCase().includes(query) ||
          it.questionRomaji.toLowerCase().includes(query) ||
          it.questionBn.toLowerCase().includes(query)
        );
      }

      container.innerHTML = list.map(it => `
        <div class="item-row-card" style="flex-direction:column; align-items:flex-start; gap:8px;">
          <div style="display:flex; justify-content:space-between; width:100%; align-items:center;">
            <span class="badge-pill" style="color:var(--accent-sakura); border-color:var(--accent-sakura);">প্রশ্ন #${it.number} • ${it.categoryBn}</span>
            <button class="speaker-btn" style="width:34px; height:34px; font-size:15px;" onclick="playAudio('${it.audioPhrase || it.questionJp}', this)">🔊</button>
          </div>
          <div style="font-size:16px; font-weight:800; color:var(--text-main);">${it.questionJp}</div>
          <div style="font-size:12px; color:var(--accent-yellow);">${it.questionRomaji}</div>
          <div style="font-size:13.5px; font-weight:700; color:var(--accent-green);">${it.questionBn}</div>

          <div style="width:100%; border-top:1px dashed var(--card-border); padding-top:8px; margin-top:4px;">
            <div style="font-size:11px; font-weight:800; color:var(--text-muted); margin-bottom:4px;">নমুনা উত্তর (Sample Answers):</div>
            ${it.answers.map(ans => `
              <div style="background:var(--card-elevated); border-radius:10px; padding:8px 10px; margin-bottom:6px; display:flex; justify-content:space-between; align-items:center;">
                <div>
                  <div style="font-size:13px; font-weight:700; color:var(--text-main);">${ans.jp}</div>
                  <div style="font-size:11px; color:var(--text-muted);">${ans.romaji}</div>
                  <div style="font-size:12px; color:var(--accent-cyan); font-weight:600;">${ans.bn}</div>
                </div>
                <button class="speaker-btn" style="width:30px; height:30px; font-size:13px;" onclick="playAudio('${ans.jp}', this)">🔊</button>
              </div>
            `).join('')}
          </div>
        </div>
      `).join('');
    }

    // -------------------------------------------------------------
    // 7. COUNTING UNITS SCREEN
    // -------------------------------------------------------------
    function renderCountingList() {
      const container = document.getElementById('countingItemsList');
      const list = ALL_ITEMS.filter(it => it.category === 'COUNTING');

      container.innerHTML = list.map(it => `
        <div class="item-row-card">
          <div>
            <div class="item-jp-text" style="color:var(--accent-coral);">${it.japanese}</div>
            <div class="item-romaji-text">${it.romaji}</div>
            <div class="item-bn-text" style="color:var(--text-main);">${it.bangla}</div>
          </div>
          <button class="speaker-btn" onclick="playAudio('${it.audioPhrase || it.japanese}', this)">🔊</button>
        </div>
      `).join('');
    }

    // -------------------------------------------------------------
    // 8. BOOKMARKS SCREEN
    // -------------------------------------------------------------
    function toggleBookmark(id, btn) {
      const idx = bookmarks.findIndex(b => b.id === id);
      if (idx !== -1) {
        bookmarks.splice(idx, 1);
        if (btn) btn.classList.remove('active');
      } else {
        const item = ALL_ITEMS.find(it => it.id === id);
        if (item) {
          bookmarks.push(item);
          if (btn) btn.classList.add('active');
        }
      }
      localStorage.setItem('jd_bookmarks', JSON.stringify(bookmarks));
      if (document.getElementById('screenBookmarks').classList.contains('active')) {
        renderBookmarksList();
      }
    }

    function renderBookmarksList() {
      const container = document.getElementById('bookmarksItemsList');
      if (bookmarks.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding:40px; color:var(--text-muted); font-size:14px;">কোনো বুকমার্ক সংরক্ষিত নেই। শব্দ বা কাঞ্জির পাশে ★ আইকনে ক্লিক করে সংরক্ষণ করুন।</div>`;
        return;
      }

      container.innerHTML = bookmarks.map(it => `
        <div class="item-row-card">
          <div class="item-main-content">
            <div class="item-jp-text">${it.japanese}</div>
            <div class="item-romaji-text">${it.romaji}</div>
            <div class="item-bn-text">${it.bangla}</div>
          </div>
          <div class="item-actions">
            <button class="speaker-btn" onclick="playAudio('${it.audioPhrase || it.japanese}', this)">🔊</button>
            <button class="star-btn active" onclick="toggleBookmark('${it.id}', this)">★</button>
          </div>
        </div>
      `).join('');
    }

    // -------------------------------------------------------------
    // 3D FLASHCARD MODAL ENGINE
    // -------------------------------------------------------------
    function openFlashcards(category) {
      if (category === 'VOCABULARY') {
        fcList = ALL_ITEMS.filter(it => it.category === 'VOCABULARY' && it.lesson === selectedVocabLesson);
        document.getElementById('fcTitle').textContent = `Lesson ${selectedVocabLesson} Flashcards`;
      } else {
        fcList = ALL_ITEMS.filter(it => it.category === 'KANJI');
        document.getElementById('fcTitle').textContent = 'Kanji Flashcards';
      }

      if (fcList.length === 0) {
        alert('No flashcards available.');
        return;
      }

      fcIndex = 0;
      document.getElementById('flashcardModal').classList.add('active');
      updateFlashcardView();
    }

    function closeFlashcards() {
      document.getElementById('flashcardModal').classList.remove('active');
    }

    function updateFlashcardView() {
      const item = fcList[fcIndex];
      const card = document.getElementById('fcCard');
      card.classList.remove('flipped');

      document.getElementById('fcProgress').textContent = `Card ${fcIndex + 1} of ${fcList.length}`;
      document.getElementById('fcFrontBadge').textContent = `LESSON ${item.lesson || 1}`;

      if (!fcIsReverse) {
        document.getElementById('fcFrontJp').textContent = item.japanese;
        document.getElementById('fcFrontRomaji').textContent = item.romaji;
        document.getElementById('fcBackBn').textContent = item.bangla;
        document.getElementById('fcBackEn').textContent = item.english || '';
        document.getElementById('fcBackOriginal').textContent = `${item.japanese} (${item.romaji})`;
      } else {
        document.getElementById('fcFrontJp').textContent = item.bangla;
        document.getElementById('fcFrontRomaji').textContent = item.english || '';
        document.getElementById('fcBackBn').textContent = item.japanese;
        document.getElementById('fcBackEn').textContent = item.romaji;
        document.getElementById('fcBackOriginal').textContent = item.bangla;
      }

      if (fcAutoVoice) {
        setTimeout(() => playCurrentFcAudio(), 250);
      }
    }

    function flipFlashcard() {
      document.getElementById('fcCard').classList.toggle('flipped');
    }

    function nextFlashcard() {
      if (fcIndex < fcList.length - 1) {
        fcIndex++;
        updateFlashcardView();
      }
    }

    function prevFlashcard() {
      if (fcIndex > 0) {
        fcIndex--;
        updateFlashcardView();
      }
    }

    function playCurrentFcAudio() {
      const item = fcList[fcIndex];
      if (item) playAudio(item.audioPhrase || item.japanese);
    }

    function toggleFcReverse() {
      fcIsReverse = !fcIsReverse;
      updateFlashcardView();
    }

    function shuffleFlashcards() {
      fcList.sort(() => Math.random() - 0.5);
      fcIndex = 0;
      updateFlashcardView();
    }

    function toggleFcAutoVoice() {
      fcAutoVoice = !fcAutoVoice;
      document.getElementById('fcAutoVoiceBtn').textContent = fcAutoVoice ? 'অটো-ভয়েস ON 🔊' : 'অটো-ভয়েস OFF 🔇';
    }

    function fcMarkMastered() {
      nextFlashcard();
    }

    function fcMarkReview() {
      nextFlashcard();
    }

    // -------------------------------------------------------------
    // INTERACTIVE QUIZ ENGINE
    // -------------------------------------------------------------
    function shuffleArray(arr) {
      const copy = [...arr];
      for (let i = copy.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [copy[i], copy[j]] = [copy[j], copy[i]];
      }
      return copy;
    }

    function openQuiz(category) {
      if (category === 'KANJI') {
        let pool = ALL_ITEMS.filter(it => it.category === 'KANJI');
        quizList = shuffleArray(pool).slice(0, 10);
      } else {
        let pool = ALL_ITEMS.filter(it => it.category === 'VOCABULARY' && it.lesson === selectedVocabLesson);
        if (pool.length < 4) pool = ALL_ITEMS.filter(it => it.category === 'VOCABULARY');
        // Include ALL words in the selected lesson in random order
        quizList = shuffleArray(pool);
      }
      quizIndex = 0;
      quizScore = 0;

      document.getElementById('quizModal').classList.add('active');
      renderQuizQuestion();
    }

    function openParticleQuiz() {
      quizList = [...PARTICLE_QUESTIONS].sort(() => Math.random() - 0.5).slice(0, 10);
      quizIndex = 0;
      quizScore = 0;
      document.getElementById('quizModal').classList.add('active');
      renderParticleQuizQuestion();
    }

    function closeQuiz() {
      document.getElementById('quizModal').classList.remove('active');
    }

    function renderQuizQuestion() {
      const item = quizList[quizIndex];
      document.getElementById('quizTitle').textContent = `Quiz • Question ${quizIndex + 1}/${quizList.length}`;
      document.getElementById('quizScoreText').textContent = `Score: ${quizScore}`;
      document.getElementById('quizPrompt').textContent = item.japanese;
      document.getElementById('quizCategoryBadge').textContent = `${item.category} • অর্থ নির্বাচন করুন`;

      const wrongOptions = ALL_ITEMS
        .filter(it => it.id !== item.id && it.bangla)
        .sort(() => Math.random() - 0.5)
        .slice(0, 3)
        .map(it => it.bangla);

      const options = [...wrongOptions, item.bangla].sort(() => Math.random() - 0.5);

      const container = document.getElementById('quizOptionsContainer');
      container.innerHTML = options.map(opt => `
        <button class="quiz-opt-btn" onclick="checkQuizAnswer(this, '${opt === item.bangla}')">${opt}</button>
      `).join('');

      playAudio(item.japanese);
    }

    function renderParticleQuizQuestion() {
      const q = quizList[quizIndex];
      document.getElementById('quizTitle').textContent = `Particle Quiz • Q${quizIndex + 1}/${quizList.length}`;
      document.getElementById('quizScoreText').textContent = `Score: ${quizScore}`;
      document.getElementById('quizPrompt').textContent = q.sentenceJp;
      document.getElementById('quizCategoryBadge').textContent = q.sentenceBangla;

      const container = document.getElementById('quizOptionsContainer');
      container.innerHTML = q.options.map((opt, i) => `
        <button class="quiz-opt-btn" onclick="checkQuizAnswer(this, '${i === q.correctIndex}')">${opt}</button>
      `).join('');
    }

    function checkQuizAnswer(btn, isCorrect) {
      if (btn.classList.contains('correct') || btn.classList.contains('wrong')) return;
      if (isCorrect === 'true') {
        btn.classList.add('correct');
        quizScore += 10;
        document.getElementById('quizScoreText').textContent = `Score: ${quizScore}`;
      } else {
        btn.classList.add('wrong');
      }

      setTimeout(() => {
        if (quizIndex < quizList.length - 1) {
          quizIndex++;
          if (quizList[0].options) renderParticleQuizQuestion();
          else renderQuizQuestion();
        } else {
          alert(`অভিনন্দন! কুইজ সম্পন্ন হয়েছে। আপনার স্কোর: ${quizScore} / ${quizList.length * 10}`);
          closeQuiz();
        }
      }, 700);
    }

    function speakQuizPrompt() {
      const item = quizList[quizIndex];
      if (item) playAudio(item.japanese || item.sentenceJp);
    }

    // Study Time Counter
    let studySeconds = parseInt(localStorage.getItem('jd_study_seconds') || '600', 10);
    setInterval(() => {
      studySeconds += 5;
      localStorage.setItem('jd_study_seconds', studySeconds);
      const mins = Math.floor(studySeconds / 60);
      const hrs = Math.floor(mins / 60);
      const txt = hrs > 0 ? `${hrs}h ${mins % 60}m` : `${mins}m`;
      const el = document.getElementById('statStudyTime');
      if (el) el.textContent = txt;
    }, 5000);

    window.addEventListener('DOMContentLoaded', () => {
      // Init
    });
  </script>
</body>
</html>'''

# Write to destinations
destinations = [
    'index.html',
    'public/index.html',
    '/.aistudio/artifacts/brain/789515e9-d57a-4d18-825e-3736c6739eaf/index.html'
]

os.makedirs('public', exist_ok=True)
os.makedirs('/.aistudio/artifacts/brain/789515e9-d57a-4d18-825e-3736c6739eaf', exist_ok=True)

for dest in destinations:
    try:
        with open(dest, 'w', encoding='utf-8') as f:
            f.write(html_template)
        print(f'Successfully wrote {dest} ({len(html_template)/1024:.1f} KB)')
    except Exception as e:
        print(f'Error writing {dest}: {e}')
