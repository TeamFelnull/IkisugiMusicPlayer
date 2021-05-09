package red.felnull.imp.client.music.player;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL11;
import red.felnull.imp.client.util.SoundMath;
import red.felnull.imp.music.resource.MusicLocation;
import red.felnull.otyacraftengine.client.util.IKSGOpenALUtil;
import red.felnull.otyacraftengine.throwable.OpenALException;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL11.*;

public class LavaALMusicPlayer extends LavaAbstractMusicPlayer {
    private final List<Integer> buffers = new ArrayList<>();
    private ByteBuffer pcm = BufferUtils.createByteBuffer(11025);
    private Vec3 position = Vec3.ZERO;
    private byte[] buffer = new byte[1024 * 3];
    private final int source;
    private boolean stopped;
    private boolean ready;
    private float ang;
    private int trig;
    private float attenuation;

    public LavaALMusicPlayer(MusicLocation location, AudioPlayerManager audioPlayerManager, AudioDataFormat dataformat) {
        super(location, audioPlayerManager, dataformat);
        this.source = alGenSources();
    }

    @Override
    public void ready(long position) throws Exception {
        super.ready(position);
        alSourcef(source, AL_PITCH, 1f);
        alSourcei(source, AL_SOURCE_RELATIVE, AL_FALSE);
        alSourcei(source, AL_LOOPING, AL_FALSE);
        IKSGOpenALUtil.checkErrorThrower();

        AudioFormat format = stream.getFormat();
        for (int i = 0; i < 500; i++) {
            if (stream.read(buffer) >= 0) {
                int bff = alGenBuffers();
                ang = fillBuffer(ang, pcm);

                int formatId = audioFormatToOpenAl(format);
                alBufferData(bff, formatId, getBuffer(buffer), (int) format.getSampleRate());
                buffers.add(bff);
                alSourceQueueBuffers(source, bff);
            }
        }
        LoadThread lt = new LoadThread();
        lt.start();

        ready = true;
    }

    @Override
    public void play(long delay) {
        if (!ready)
            return;

        super.play(delay);

        if (duration == 0 || duration >= startPosition) {
            float secdelay = delay / 1000f;
            alSourcef(source, AL_SEC_OFFSET, secdelay);
            alSourcePlay(this.source);
        }
    }

    @Override
    public void stop() {
        if (ready)
            alSourceStop(this.source);
    }

    @Override
    public void setPosition(long position) {
        if (audioPlayer.getPlayingTrack() != null) {
            audioPlayer.getPlayingTrack().setPosition(position);
        }
    }

    @Override
    public void destroy() {
        stopped = true;
        super.destroy();
        if (ready) {
            alSourceStop(source);
            alDeleteSources(source);
            List<Integer> bffs = new ArrayList<>(buffers);
            bffs.forEach(AL11::alDeleteBuffers);
            buffers.clear();
            try {
                IKSGOpenALUtil.checkErrorThrower();
            } catch (OpenALException e) {
                e.printStackTrace();
            }
        }
    }

    private int getPlayState() {
        return !ready ? AL_STOPPED : alGetSourcei(this.source, AL_SOURCE_STATE);
    }

    @Override
    public void pause() {
        if (getPlayState() == AL_PLAYING) {
            alSourcePause(this.source);
        }
    }

    @Override
    public void unpause() {
        if (getPlayState() == AL_PAUSED) {
            alSourcePlay(this.source);
        }
    }

    @Override
    public boolean playing() {
        return getPlayState() == AL_PLAYING;
    }

    @Override
    public boolean stopped() {
        return getPlayState() == AL_STOPPED;
    }

    @Override
    public void setSelfPosition(Vec3 sp) {
        position = sp;
        alSource3f(source, AL_POSITION, (float) sp.x, (float) sp.y, (float) sp.z);
    }

    @Override
    public void setVolume(float f) {
        if (stereo) {
            f = SoundMath.calculatePseudoAttenuation(position, attenuation, f);
        }
        alSourcef(source, AL_GAIN, f);
    }

    @Override
    public void linearAttenuation(float f) {
        attenuation = f;
        alSourcei(source, AL_DISTANCE_MODEL, 53251);
        alSourcef(source, AL_MAX_DISTANCE, f);
        alSourcef(source, AL_ROLLOFF_FACTOR, 1.0F);
        alSourcef(source, AL_REFERENCE_DISTANCE, 0.0F);
    }

    @Override
    public void disableAttenuation() {
        alSourcei(this.source, AL_DISTANCE_MODEL, AL_FALSE);
    }


    public class LoadThread extends Thread {
        public LoadThread() {
            setName("LavaPlayer Load Thread");
        }

        @Override
        public void run() {
            try {
                while (stream.read(buffer) >= 0) {
                    if (stopped)
                        return;
                    int b = alGenBuffers();
                    if (stopped)
                        return;
                    ang = fillBuffer(ang, pcm);
                    if (stopped)
                        return;
                    AudioFormat format = stream.getFormat();
                    if (stopped)
                        return;
                    int formatId = audioFormatToOpenAl(format);
                    if (stopped)
                        return;
                    alBufferData(b, formatId, getBuffer(buffer), (int) format.getSampleRate());
                    if (stopped)
                        return;
                    alSourceQueueBuffers(source, b);
                    if (stopped)
                        return;
                    buffers.add(b);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public float fillBuffer(float ang, ByteBuffer buff) {
        int size = buff.capacity();
        trig++;
        for (int i = 0; i < size; i++) {
            int source1 = (int) (Math.sin(ang) * 127 + 128);
            int source2 = 0;
            if (trig > 3) source2 = (int) (Math.sin(ang * 3) * 127 + 128);
            if (trig > 4) trig = 0;
            buff.put(i, (byte) ((source1 + source2) / 2));
            ang += 0.1f;
        }
        return ang;
    }


    public ByteBuffer getBuffer(byte[] array) {
        ByteBuffer audioBuffer2 = BufferUtils.createByteBuffer(array.length);
        audioBuffer2.put(array);
        audioBuffer2.flip();
        return audioBuffer2;
    }

    private static int audioFormatToOpenAl(AudioFormat audioFormat) {
        AudioFormat.Encoding encoding = audioFormat.getEncoding();
        int i = audioFormat.getChannels();
        int j = audioFormat.getSampleSizeInBits();
        if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED) || encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (i == 1) {
                if (j == 8) {
                    return AL_FORMAT_MONO8;
                }

                if (j == 16) {
                    return AL_FORMAT_MONO16;
                }
            } else if (i == 2) {
                if (j == 8) {
                    return AL_FORMAT_STEREO8;
                }

                if (j == 16) {
                    return AL_FORMAT_STEREO16;
                }
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + audioFormat);
    }
}