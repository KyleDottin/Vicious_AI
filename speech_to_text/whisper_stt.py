import whisper
import shutil

ffmpeg_path = shutil.which("ffmpeg")
if ffmpeg_path is None:
    raise EnvironmentError("ffmpeg not found.")

model = whisper.load_model("base")

def transcribe(audio_path):

    result = model.transcribe(
        audio_path,
        language="en",
        fp16=False
    )

    return result["text"]


if __name__ == "__main__":
    audio = "audio/test.wav"
    print(transcribe(audio))