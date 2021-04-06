package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * Audio class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Audio { 
   
    public static int SOUND_NPC_DIALOG = 1;
    public static int SOUND_MENU_CONFIRMED = 2;
    public static int SOUND_TORCH_USED = 3;
    public static int SOUND_BLOCKED = 4;
    public static int SOUND_SWAMP = 5;
    public static int SOUND_PLAYER_HIT = 6;
    public static int SOUND_PLAYER_WILL_ATTACK = 7;
    public static int SOUND_ENEMY_WILL_ATTACK = 8;
    public static int SOUND_ENEMY_DODGING = 9;
    public static int SOUND_ENEMY_MISSED_ATTACK = 10;
    public static int SOUND_BARRIER_XXX = 11; // ???
    public static int SOUND_ENEMY_HIT = 12;
    public static int SOUND_CHEST_OPENED = 13;
    public static int SOUND_SHOW_OPTIONS_MENU = 14;
    public static int SOUND_DOOR_OPENED = 15;
    public static int SOUND_CRITICAL_ATTACK = 16;
    public static int SOUND_RUN_AWAY = 17;
    public static int SOUND_SPELL = 18;
    public static int SOUND_ENTRANCE_OR_STAIRS = 19;
    public static int SOUND_DRAGON_LORD_BREATHING_FIRE = 20;
    public static int SOUND_WYVERN_WING = 21;
    public static int SOUND_BATTLE_WIN = 22;
    public static int SOUND_LEVEL_UP = 23;
    public static int SOUND_SILVER_HARP = 24;
    public static int SOUND_FAIRY_FLUTE = 25;
    public static int SOUND_CURSED = 26;
    public static int SOUND_RAINBOW_BRIDGE = 27;
    public static int SOUND_PRINCESS_RESCUED = 28;
    public static int SOUND_INN = 29;
    public static int SOUND_PLAYER_DEAD = 30;
    
    private static Synthesizer musicSynth;
    private static boolean musicInitialized;
    private static Sequencer sequencer;
    private static int musicVolume;
    private static Music currentMusic;
    
    private static boolean soundInitialized;
    private static Synthesizer synth;
    private static int soundVolume;
    
    //id midi_file start_tick_position loop_start_tick loop_end_tick        
    public static class Music {
        public String id;
        public Sequence sequence;
        public long startTickPosition;
        public long loopStartTick;
        public long loopEndTick;
        public int loopCount;
        
        public Music(String serializedData) {
            String[] args = serializedData.trim().split(",");
            String[] h = args[0].trim().split("\\s+");
            id = h[1].trim();
            sequence = Resource.loadMusicSequence(args[1].trim());
            startTickPosition = Long.parseLong(args[2].trim());
            loopStartTick = Long.parseLong(args[3].trim());
            loopEndTick = Long.parseLong(args[4].trim());
            loopCount = Integer.parseInt(args[5].trim());
        }        
    }
    
    public static void start() {
        try {
            musicSynth = MidiSystem.getSynthesizer();
            musicSynth.open();
            Soundbank msb = Resource.loadSoundBank(RES_SOUND_BANK);
            musicSynth.loadAllInstruments(msb);
            setMusicVolume(4);
             
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            sequencer.getTransmitter().setReceiver(musicSynth.getReceiver());
                    
            musicInitialized = true;
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            musicInitialized = false;
        }
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            Soundbank sb = Resource.loadSoundBank(RES_SOUND_EFFECTS);
            synth.loadAllInstruments(sb);
            soundInitialized = true;
            setSoundVolume(7);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            soundInitialized = false;
        }
    }

    public static int getSoundVolume() {
        return soundVolume;
    }
    
    // volume 0~9
    @ScriptCommand(name = "set_sound_volume")
    public static void setSoundVolume(int volume) {
        MidiChannel[] channels = synth.getChannels();
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(7, (int) (127 * (volume / 9.0)));
            channels[i].controlChange(39, (int) (127 * (volume / 9.0)));
        }  
        soundVolume = volume;
        Script.setGlobalValue("##game_config_sound_volume", volume);
    }
    
    @ScriptCommand(name = "play_sound")
    public static void playSound(int soundId) {
        if (soundInitialized) {
            synth.getChannels()[1].noteOff(soundId, 200);
            synth.getChannels()[1].noteOn(soundId, 200);
        }
    }

    public static Music getCurrentMusic() {
        return currentMusic;
    }

    public static int getMusicVolume() {
        return musicVolume;
    }
    
    // volume 0~9
    @ScriptCommand(name = "set_music_volume")
    public static void setMusicVolume(int volume) {
        MidiChannel[] channels = musicSynth.getChannels();
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(7, (int) (127 * (volume / 9.0)));
            channels[i].controlChange(39, (int) (127 * (volume / 9.0)));
        }  
        musicVolume = volume;
        Script.setGlobalValue("##game_config_music_volume", volume);
    }
    
    @ScriptCommand(name = "play_music")
    public static void playMusic(String musicId) {
        try {
            if (musicInitialized) {
                Music music = Resource.getMusic(musicId);
                Audio.currentMusic = music;
                sequencer.stop();
                sequencer.setSequence(music.sequence);
                //System.out.println("tick length: " 
                //                              + sequencer.getTickLength());
                sequencer.setLoopStartPoint(0);
                sequencer.setLoopEndPoint(0);
                sequencer.setTickPosition(0);
                sequencer.setLoopEndPoint(music.loopEndTick);
                sequencer.setLoopStartPoint(music.loopStartTick);
                sequencer.setTickPosition(music.startTickPosition);
                sequencer.setLoopCount(music.loopCount);
                sequencer.start();
            }
        } 
        catch (InvalidMidiDataException ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            musicInitialized = false;
        }
    }

    @ScriptCommand(name = "pause_music")
    public static void pauseMusic() {
        if (musicInitialized) {
            sequencer.stop();
        }
    }

    @ScriptCommand(name = "resume_music")
    public static void resumeMusic() {
        if (musicInitialized) {
            sequencer.start();
        }
    }
    
    @ScriptCommand(name = "stop_music")
    public static void stopMusic() {
        if (musicInitialized) {
            sequencer.stop();
        }
        currentMusic = null;
    }

    private static String savedCurrentMusicId;
    
    @ScriptCommand(name = "save_current_music")
    public static void saveCurrentMusic() {
        savedCurrentMusicId = currentMusic.id;
    }
    
    @ScriptCommand(name = "restore_play_saved_music")
    public static void restoreAndPlaySavedMusic() {
        playMusic(savedCurrentMusicId);
    }

    public static void applyConfigValues() {
        Integer configMusicVolume = 5;
        try {
            configMusicVolume = (Integer) 
                    Script.getGlobalValue("##game_config_music_volume");
        }
        catch (Exception e) { }
        Integer configSoundVolume = 5;
        try {
            configSoundVolume = (Integer) 
                    Script.getGlobalValue("##game_config_sound_volume");
        }
        catch (Exception e) { }
        setMusicVolume(configMusicVolume);
        setSoundVolume(configSoundVolume);
    }
    
    public static void main(String[] args) {
        Resource.loadMusics("musics");
        Audio.start();
        //Audio.playMusic("tantegel");//ok
        //Audio.playMusic("world");//ok
        //Audio.playMusic("battle");//ok
        //Audio.playMusic("boss");//ok
        //Audio.playMusic("intro");//ok
        Audio.playMusic("ending");//ok
        Audio.setMusicVolume(40);
    }
    
}
