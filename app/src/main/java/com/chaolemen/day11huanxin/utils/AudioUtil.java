package com.chaolemen.day11huanxin.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioUtil {

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 44100;

    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final String TAG = "AudioUtil";
    private static AudioRecord audioRecord;
    private static String mPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/录音/";
    public static boolean isRecording;
    private static long mAudioName;
    private static long sEndTime;
    private static long sDuration;

    /**
     * 开始录音
     * @param callBack
     */
    public static void startRecord(final ResultCallBack callBack) {
        mAudioName = System.currentTimeMillis();
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        final File file = new File(mPath, mAudioName+".pcm");
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }
        //录音
        audioRecord.startRecording();
        isRecording = true;

        //将录音写入本地文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "run: close file output stream !");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        callBack.onFail(e.toString());
                    }

                    sEndTime = System.currentTimeMillis();
                    sDuration = sEndTime - mAudioName;
                    //将PCM格式的录音转换为wav的,方便播放
                    PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                    File pcmFile = new File(mPath, mAudioName+".pcm");
                    File wavFile = new File(mPath, mAudioName+".wav");
                    if (!wavFile.mkdirs()) {
                        Log.e(TAG, "wavFile Directory not created");
                    }
                    if (wavFile.exists()) {
                        wavFile.delete();
                    }
                    pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
                    //将结果回调给调用者
                    callBack.onSuccess(wavFile.getAbsolutePath(),sDuration);
                }
            }
        }).start();
    }

    /**
     * 停止录音
     */
    public static void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            //recordingThread = null;
        }
    }

    /**
     * 将pcm音频文件转换为wav音频文件
     */
    public static class PcmToWavUtil {

        /**
         * 缓存的音频大小
         */
        private static int mBufferSize;
        /**
         * 采样率
         */
        private static int mSampleRate;
        /**
         * 声道数
         */
        private static int mChannel;

        /**
         * @param sampleRate sample rate、采样率
         * @param channel channel、声道
         * @param encoding Audio data format、音频格式
         */
        PcmToWavUtil(int sampleRate, int channel, int encoding) {
            this.mSampleRate = sampleRate;
            this.mChannel = channel;
            this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encoding);
        }


        /**
         * pcm文件转wav文件
         *
         * @param inFilename 源文件路径
         * @param outFilename 目标文件路径
         */
        public static void pcmToWav(String inFilename, String outFilename) {
            FileInputStream in;
            FileOutputStream out;
            long totalAudioLen;
            long totalDataLen;
            long longSampleRate = mSampleRate;
            int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
            long byteRate = 16 * mSampleRate * channels / 8;
            byte[] data = new byte[mBufferSize];
            try {
                in = new FileInputStream(inFilename);
                out = new FileOutputStream(outFilename);
                totalAudioLen = in.getChannel().size();
                totalDataLen = totalAudioLen + 36;

                writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                        longSampleRate, channels, byteRate);
                while (in.read(data) != -1) {
                    out.write(data);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * 加入wav文件头
         */
        private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                                long totalDataLen, long longSampleRate, int channels, long byteRate)
                throws IOException {
            byte[] header = new byte[44];
            // RIFF/WAVE header
            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            //WAVE
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            // 'fmt ' chunk
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            // 4 bytes: size of 'fmt ' chunk
            header[16] = 16;
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            // format = 1
            header[20] = 1;
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            // block align
            header[32] = (byte) (2 * 16 / 8);
            header[33] = 0;
            // bits per sample
            header[34] = 16;
            header[35] = 0;
            //data
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            out.write(header, 0, 44);
        }
    }
}
