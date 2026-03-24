import requests
import json

OLLAMA_URL = "http://localhost:11434/api/generate"

def analyse_call(text):

    prompt = f"""
Analyse cette transcription d'appel et détermine s'il s'agit d'une arnaque.

TRANSCRIPTION: "{text}"

Réponds uniquement en JSON :
{{
 "verdict": "SCAM ou LEGITIME",
 "score_risque": 0-10,
 "raison": "explication"
}}
"""

    r = requests.post(
        OLLAMA_URL,
        json={
            "model": "llama3.1:8b",
            "prompt": prompt,
            "stream": False,
            "format": "json"
        }
    )

    return json.loads(r.json()["response"])