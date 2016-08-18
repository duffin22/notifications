package ly.generalassemb.drewmahrt.stockpriceclient;

/**
 * Created by matthewtduffin on 18/08/16.
 */
public class RedditListing {
    private String subreddit;
    private String title;
    private int score;
    private boolean over18;

    public RedditListing(String subreddit, String title, int score, boolean over18) {
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
