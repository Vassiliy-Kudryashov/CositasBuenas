package cositas.buenas.getlog;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static cositas.buenas.getlog.GetlogUtil.parseEmail;

class Author implements Comparable<Author> {
    Set<String> raws = new TreeSet<>();
    String nameAndSurname = "";
    Set<String> emails = new TreeSet<>();

    public Author(String raw, String nameAndSurname) {
        raws.add(raw);
        this.nameAndSurname = nameAndSurname;
        Optional.ofNullable(parseEmail(raw)).ifPresent(s -> emails.add(s));
    }

    void merge(Author author) {
        this.raws.addAll(author.raws);
        this.emails.addAll(author.emails);
    }

    @Override
    public String toString() {
        return nameAndSurname;
    }

    @Override
    public int compareTo(Author other) {
        return nameAndSurname.compareTo(other.nameAndSurname);
    }

    boolean isBot() {
        if (nameAndSurname.startsWith("No Reply")) return true;
        if (nameAndSurname.startsWith("No_reply")) return true;
        if (nameAndSurname.toLowerCase().endsWith("robot")) return true;
        if (nameAndSurname.toLowerCase().contains("updater")) return true;
        if (nameAndSurname.contains("[bot]")) return true;
        if (nameAndSurname.contains("builduser")) return true;
        return false;
    }

}
