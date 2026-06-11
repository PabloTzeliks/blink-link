package pablo.tzeliks.blink_link.application.url.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;
    private final int limitApplied;
    private final int remainingRequests;

    public RateLimitExceededException(String msg, long retryAfterSeconds, int limitApplied, int remainingRequests) {
        super(msg);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitApplied = limitApplied;
        this.remainingRequests = remainingRequests;
    }

    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public int getLimitApplied() { return limitApplied; }
    public int getRemainingRequests() { return remainingRequests; }
}
