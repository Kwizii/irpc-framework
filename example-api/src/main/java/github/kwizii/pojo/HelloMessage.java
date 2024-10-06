package github.kwizii.pojo;


import java.io.Serializable;

public class HelloMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;

    public HelloMessage() {
    }

    public HelloMessage(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "HelloMessage{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
