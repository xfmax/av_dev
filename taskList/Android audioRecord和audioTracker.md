task：在 Android 平台使用 AudioRecord 和 AudioTrack API 完成音频 PCM 数据的采集和播放，并实现读写音频 wav 文件。
首先，我们有几个概念要先了解一下：

采样率：一秒钟可以采集的采样点数量。

采样位数：一个采样点能采集的数量，通常有8位和16位，分别可以容纳2的8次方和2的16次方个数据。

声道数：有单声道和双声道，甚至有些设备支持多声道。

PCM：一种声音原始采集的格式标准，没有进行任何的压缩处理，可以理解为声音的原始文件，包含的声音最为完整的一种格式。

这样基本的概念介绍的就差不多了，接下来我们来看看Android中如何来采集与播放一段音频：

Android中有两个基础的操控音频的api可以使用，分别是AudioRecord和AudioTrack，首先来看看AudioRecord:

```java

    private void record() {

        if (isRecroding) {
            Toast.makeText(MainActivity.this, "正在录制中...", Toast.LENGTH_SHORT).show();
        } else {
            record.setText("开始");
            isRecroding = true;

            //根据上面介绍的采样率、声道数、采样位数（编码位数）创建一个缓存区，并返回缓冲区的大小，从这里可以看出采集是一段一段采集的.
            final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
            //创建一个audioRecord对象，创建的参数MediaRecorder.AudioSource.MIC（通过麦克风采集，注意这里使用的是MediaRecord）。
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                    CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
            //创建此抽象路径指定的目录，包括所有必须但不存在的父目录。
            if (file.mkdirs()) {
                Log.d(TAG, "dir 存在！");
            }

            if (file.exists()) {
                file.delete();
            }
            final byte[] data = new byte[minBufferSize];
            //开启录制，这里可以发现，你可以先开启录制，再启动线程往文件里写数据。
            audioRecord.startRecording();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);

                        if (fileOutputStream != null) {

                            while (isRecroding) {
                                //使用audioRecord的read方法，从读取minBufferSize大小的数据，返回的一个类似于code码的int型整数值，这个值可以表明read操作是否为有效操作，如果有效就可以往硬盘上执行写操作。
                                if (audioRecord.read(data, 0, minBufferSize) != AudioRecord.ERROR_INVALID_OPERATION) {
                                    fileOutputStream.write(data);
                                }
                            }

                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (null != audioRecord) {
                            audioRecord.stop();
                            audioRecord.release();
                        }
                    }
                }
            }).start();
        }
    }
```
整体就是先根据采样频率、采样位数、声道数获取一个minBefferedSize,这个size决定了audioRecord一次可以读取多少数据量，而在底层，这个缓冲是以一个叫Frame（帧）的方式存在的，Frame的计算方式是 采样位数 × 声道数 / 8，最后得到的数据单位是byte（字节），代表的意义是：一次采样可以采集到多少数据，举个例子：如果是16位的采样位数，双通道，那么Frame的大小就是 16 × 2 / 8 = 4 字节，也就是这种设备一次可以采集4字节的数据量，如果再延展一下，给出采样频率（1秒钟可以采集几次），就可以算出1秒钟可以采集的数据量是多少了，ok，是不是很简单呢，采集过后，你会在sdcard上看到一个test.pcm的音频文件，但是如果这时你尝试通过系统自带的音乐播放器打开文件，你会发现打不开，为什么？因为没有头文件，这里面没有包含任何的解析信息，音乐播放器不知道这个东西是干什么的，以什么形式编码的，所以不能播放，如果你想让他能播放，可以考虑给它加一个header，这样pcm文件就可以转换为wav文件了，大家应该都用过wav格式的文件，它的兼容性好，支持各平台大多数的播放软件，因为这个wav只是对pcm添加了一个头文件，它没有进行编码，基本上还是原始文件，所以播放器都可以打开它，但是缺点就是原始文件的容量太大，有些信息甚至对于大部分用户是没有价值的，所以才有后来各种编解码的格式出现，为的就是解决占用容量太大，而很多普通用户使用不上这么大的数据的问题，这都是后话了，但是大家要知道诞生编解码的原因。

接下来，录制完成就应该是播放了：

```java
private void play() {

        if (isPlay) {
            Toast.makeText(MainActivity.this, "正在播放中...", Toast.LENGTH_SHORT).show();
        } else {
            isPlay = true;
            //一样的配方一样的味道，但是请注意这里的声道从采集时的CHANNEL_IN_MONO换成了CHANNEL_OUT_MONO，这点要注意，因为这里是输出
            int minBufferSize = AudioTrack.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //创建audioTrack，看看第一个参数AudioAttributes,它负责配置这个要播放的文件的用途（usage）与类型(contentType)和行为（flags），对应了为什么要播放这个音频文件和播放的是什么东西以及怎么播放这个音频文件的三个问题，第三个参数flags不好理解，我举个例子FLAG_HW_AV_SYNC这个标签代表了请求一个支持硬件av同步的输出流。
            audioTrack = new AudioTrack(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                    new AudioFormat.Builder().setSampleRate(SAMPLING_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                    minBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
            final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

            if (file.exists()) {
                Log.d(TAG, "file exist");
            }

            final byte[] data = new byte[minBufferSize];
            audioTrack.play();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        try {
                            //循环读取文件，使用available从文件里查看是否还有可以读取的数据
                            while (fis.available() > 0) {
                                int code = fis.read(data);
                                //判断数据的有效性
                                if (code == AudioTrack.ERROR_BAD_VALUE || code == AudioTrack.ERROR_INVALID_OPERATION) {
                                    continue;
                                }
                                //判断数据不为空
                                if (code != 0 && code != -1) {
                                    audioTrack.write(data, 0, code);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();

        }

    }

```
AudioTrack是一个播放音频的Api，需要将数据写入到其中来进行播放，其实还是围绕着采样频率、采样位数、声道数来创建这个对象，底层会使用AudioFlinger将多个AudioTrack进行mix，并通过硬件渲染给声卡进行播放，android系统可以支持同时mix 32个AudioTrack，所以了解AudioFlinger也是势在必行的事，后面会慢慢涉及到。


最后的最后，在Android播放一段音频的方式除了AudioTrack，还有SoundPool和MediaPlayer,MediaPlayer底层会直接调用AudioTrack，它更适合于长时间的播放音频文件，因为加入了更多控制管理的Api，更加方便使用，SoundPool的使用场景是那些简短的音频文件，比如提示音等，这两个api在后续的文章中还有会介绍，先在此埋下伏笔。