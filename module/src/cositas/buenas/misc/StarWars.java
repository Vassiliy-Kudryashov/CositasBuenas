package cositas.buenas.misc;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

import static javax.sound.sampled.FloatControl.Type.BALANCE;

public class StarWars {
    public static void main(String[] args) {
        try (Synthesizer s = MidiSystem.getSynthesizer()) {
            s.open();
            MidiChannel ch = s.getChannels()[0];
            FloatControl balance = getBalanceControl();
            if (balance == null) {
                System.err.println("Sorry, I lost my balance");
                System.exit(42);
            }
            s.loadAllInstruments(s.getDefaultSoundbank());
            Instrument request = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("ergun")).findFirst().get();
            Instrument response = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("eaming")).findFirst().get();
            Instrument errorCode = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("ghing")).findFirst().get();

            LinkedList<Integer> queue = new LinkedList<>();
            double level = 0.5;
            double speed = 1;
            final double step = 1.05;
            while (true) {
                level = Math.min(1, Math.max(0, level * speed));
                balance.setValue(-1 + 2 * (float)level);

                if (Math.random() > level) {
                    speed *= step;
                    LockSupport.parkUntil(System.currentTimeMillis() + (int) (1000 * level));
                    continue;
                } else {
                    speed /= step;
                    speed /= step;
                }
                submit(ch, request, 52 + (int) (Math.random() * 20), 30 + (int)(70 * Math.abs(1 - Math.abs(.5 - level))), queue);
                LockSupport.parkUntil(System.currentTimeMillis() + (int) (250 * level));
                submit(ch, response, 52 + (int) (Math.random() * 20), 30 + (int)(70 *  Math.abs(1 - Math.abs(.5 - level))), queue);
                if (Math.random() > 0.75) {
                    submit(ch, errorCode, 35 + (int) (Math.random() * 40), 60 + (int)(40 * Math.abs(1 - Math.abs(.5 - level))), queue);
                }
                LockSupport.parkUntil(System.currentTimeMillis() + (int) (200 * level));
                while (queue.size() > 18) {
                    ch.noteOff(queue.removeFirst());
                }
            }
        } catch (MidiUnavailableException e1) {
            System.err.println("Sorry, I lost my noise");
        } catch (NoSuchElementException e2) {
            System.err.println("Sorry, I lost my voice");
        }
    }

    private static FloatControl getBalanceControl() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : infos) {
            for (Line line : AudioSystem.getMixer(info).getSourceLines()) {
                try {
                    return  (FloatControl)line.getControl(BALANCE);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    private static void submit(MidiChannel channel, Instrument instrument, int note, int level, List<Integer> queue) {
        channel.programChange(instrument.getPatch().getBank(), instrument.getPatch().getProgram());
        channel.noteOn(note, level);
        queue.add(note);
    }
}