from speech_to_text.whisper_stt import transcribe
from analysis.analyse_llm import analyse_call

audio_file = "audio/test.wav"

print("Transcription en cours...")

text = transcribe(audio_file)

print("Texte :", text)

print("Analyse IA...")

result = analyse_call(text)

print("\nRésultat :")
print(result)