import whisper
import shutil

_model_cache: dict = {}

def transcribe_audio_to_text(audio_path: str, model_size: str = "medium", language: str = "fr", fp16: bool = False) -> str:
    """
    Transcribe an audio into a text with Whisper.

    Args:
        audio_path:  Path to the audio file to transcribe
        model_size:  Size of the Whisper model to use (ex: "tiny", "base", "small", "medium", "large")
        language:    Language of the audio (ex: "en" for English)
        fp16:        Whether to use half-precision (fp16) for faster inference on compatible hardware. Default is False.

    Returns:
        The transcribed text

    Requires:
        pip install torch openai-whisper
        ffmpeg installed and in system PATH
        numpy 2.2.3 (not 2.2.4)
    """
    ffmpeg_path = shutil.which("ffmpeg")
    if ffmpeg_path is None:
        raise EnvironmentError("ffmpeg not found. Please install ffmpeg and ensure it's in your system PATH.")

    # Charge le modèle une seule fois et le garde en mémoire
    if model_size not in _model_cache:
        print(f"[Whisper] Chargement du modèle '{model_size}'...")
        _model_cache[model_size] = whisper.load_model(model_size)

    model = _model_cache[model_size]

    result = model.transcribe(
        audio_path,
        language=language,
        fp16=fp16,
        temperature=0.0,
        condition_on_previous_text=True
    )

    return result["text"]

def load_model(model_size: str = "medium") -> None:
    """Précharge le modèle Whisper en mémoire."""
    if model_size not in _model_cache:
        print(f"[Whisper] Chargement du modèle '{model_size}'...")
        _model_cache[model_size] = whisper.load_model(model_size)
        print(f"[Whisper] Modèle '{model_size}' prêt.")


if __name__ == "__main__":
    text = transcribe_audio_to_text("audio-wav-16khz_1002976_normalized_noise.wav")
    print("Texte transcrit :")
    print(text)