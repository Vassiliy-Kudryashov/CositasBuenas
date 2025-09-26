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
            FloatControl earGear = findTheBalance();
            if (earGear == null) {
                System.err.println("Sorry, I lost my balance");
                System.exit(42);
            }
            s.loadAllInstruments(s.getDefaultSoundbank());
            Instrument request = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("ergun")).findFirst().get();
            Instrument response = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("eaming")).findFirst().get();
            Instrument errorCode = Arrays.stream(s.getAvailableInstruments()).filter(i -> i.getName().contains("ghing")).findFirst().get();

            LinkedList<Integer> queueOfEverything = new LinkedList<>();
            double levelOfEverything = 0.5;
            double speedOfEverything = 1;
            final double step = 1.05;
            while (true) {
                levelOfEverything = Math.min(1, Math.max(0, levelOfEverything * speedOfEverything));
                earGear.setValue(-1 + 2 * (float)levelOfEverything);

                if (Math.random() > levelOfEverything) {
                    speedOfEverything *= step;
                    LockSupport.parkUntil(System.currentTimeMillis() + (int) (1000 * levelOfEverything));
                    continue;
                } else {
                    speedOfEverything /= step;
                    speedOfEverything /= step;
                }
                double ohMyAbs = Math.abs(1 - Math.abs(.5 - levelOfEverything));
                feastBothEars(ch, request, 40 + (int) (Math.random() * 30), 30 + (int)(70 * ohMyAbs), queueOfEverything);
                if (Math.random() > 0.5) {
                    LockSupport.parkUntil(System.currentTimeMillis() + (int) (250 * levelOfEverything));
                    feastBothEars(ch, response, 32 + (int) (Math.random() * 44), 30 + (int) (70 * ohMyAbs), queueOfEverything);
                    if (Math.random() > 0.5) {
                        feastBothEars(ch, errorCode, 35 + (int) (Math.random() * 40), 60 + (int)(40 * ohMyAbs), queueOfEverything);
                    }
                    LockSupport.parkUntil(System.currentTimeMillis() + (int) (200 * levelOfEverything));
                }
                while (queueOfEverything.size() > 18) {
                    ch.noteOff(queueOfEverything.removeFirst());
                }
            }
        } catch (MidiUnavailableException e1) {
            System.err.println("Sorry, I lost my noise");
        } catch (NoSuchElementException e2) {
            System.err.println("Sorry, I lost my voice");
        }
    }

    private static FloatControl findTheBalance() {
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

    private static void feastBothEars(MidiChannel channel, Instrument instrument, int note, int ohMyAbs, List<Integer> queue) {
        channel.programChange(instrument.getPatch().getBank(), instrument.getPatch().getProgram());
        channel.noteOn(note, ohMyAbs);
        queue.add(note);
    }
}