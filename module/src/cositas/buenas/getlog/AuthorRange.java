package cositas.buenas.getlog;

import java.util.Date;
import java.util.TreeSet;

class AuthorRange implements Comparable<AuthorRange> {
    final Author author;
    final TreeSet<Date> dates = new TreeSet<>();
    Date firstDate;
    Date lastDate;

    public AuthorRange(Author author, Date date) {
        this.author = author;
        this.firstDate = date;
        this.lastDate = date;
    }

    void merge(AuthorRange other) {
        this.author.merge(other.author);
        other.dates.forEach(this::addDate);
    }

    public void addDate(Date date) {
        dates.add(date);
        if (firstDate.after(date)) firstDate = date;
        if (lastDate.before(date)) lastDate = date;
    }

    public int getDays() {
        return (int) Math.max(1, ((lastDate.getTime() - firstDate.getTime()) / 86400000L));
    }

    public double getCommitsPerDay() {
        return (double) dates.size() / getDays();
    }

    @Override
    public int compareTo(AuthorRange o) {
        int i = firstDate.compareTo(o.firstDate);
        if (i != 0) return i;
        return -lastDate.compareTo(o.lastDate);
    }

    @Override
    public String toString() {
        return author + " " + Getlog.SIMPLE_FORMAT.format(firstDate) + " - " + Getlog.SIMPLE_FORMAT.format(lastDate);
    }
}
