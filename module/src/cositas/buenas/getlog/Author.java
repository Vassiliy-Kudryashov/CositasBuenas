package cositas.buenas.getlog;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static cositas.buenas.getlog.GetlogUtil.parseEmail;

class Author implements Comparable<Author> {
    int mergeRank = 0;
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
        if (author.nameAndSurname.length() > nameAndSurname.length() || (author.nameAndSurname.contains(" ") && !nameAndSurname.contains(" "))) {
            nameAndSurname = author.nameAndSurname;
        }
        mergeRank++;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int stars = raws.size() - 1 + emails.size() - 1;
        for (int i = 0; i < mergeRank; i++) sb.append("*");

        sb.append("[").append(nameAndSurname).append("]: (");
        for (Iterator<String> iterator = raws.iterator(); iterator.hasNext(); ) {
            String raw = iterator.next();
            sb.append(raw);
            if (iterator.hasNext()) {
                sb.append(", ");
            } else {
                sb.append("); ");
            }
        }
        if (!emails.isEmpty()) {
            sb.append("{");
            for (Iterator<String> iterator = emails.iterator(); iterator.hasNext(); ) {
                String email = iterator.next();
                sb.append(email);
                if (iterator.hasNext()) {
                    sb.append(", ");
                } else {
                    sb.append("}");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Author other) {
        return nameAndSurname.compareTo(other.nameAndSurname);
    }

    public boolean isSimilarTo(Author author) {
        if (nameAndSurname.equalsIgnoreCase(author.nameAndSurname)) return true;
        for (String email : author.emails) {
            String localPart = getLocalPart(email);
            if (localPart != null) {
                if (emails.contains(email) || emails.contains(email.replace('-', '.'))) {
                    return true;
                }
                String[] words = nameAndSurname.split(" ");
                if (words.length>1) {
                    if (localPart.equals(""+words[0].charAt(0)+words[words.length-1])) return true;
                }
            }
        }
        for (String email : emails) {
            String localPart = getLocalPart(email);
            if (localPart != null) {
                if (author.emails.contains(email) || author.emails.contains(email.replace('-','.'))) {
                    return true;
                }
                String[] words = author.nameAndSurname.split(" ");
                if (words.length>1) {
                    if (localPart.equals(""+words[0].charAt(0)+words[words.length-1])) return true;
                }
            }
        }
        if (!nameAndSurname.contains(" ")) {
            for (String email : author.emails) {
                if (nameAndSurname.equalsIgnoreCase(getLocalPart(email))) return true;
            }
        }
        if (!author.nameAndSurname.contains(" ")) {
            for (String email : emails) {
                if (author.nameAndSurname.equalsIgnoreCase(getLocalPart(email))) return true;
            }
        }
        return false;
    }

    private static String getLocalPart(String email) {
        int i = email.indexOf('@');
        if (i < 0) return null;
        String result = email.substring(0, i);
        return result.equalsIgnoreCase("no_reply") ? null : result;
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
