package my.test.pack;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DashboardNotifications implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static String ID = "ID";
  private static String START_TIME = "START_TIME";
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private static String notificationUrl = System.getenv("DASHBOARD_URL");
  private static String jobId = System.getenv("JOB_ID");
  private static OkHttpClient client = new OkHttpClient();
  private static ObjectMapper mapper = new ObjectMapper();

  private ExtensionContext.Store getStore(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    if (notificationUrl != null && jobId != null) {
      Map<String, Object> map = new HashMap<>();
      map.put("name", context.getDisplayName());
      map.put("testclass", context.getRequiredTestClass().getCanonicalName());
      map.put("testcase", context.getRequiredTestMethod().getName());
      map.put("job_id", jobId);
      long startedAt = System.currentTimeMillis();
      map.put("started_at", startedAt);

      String id = notify(map, notificationUrl);
      if (id != null) {
        getStore(context).put(ID, id);
        getStore(context).put(START_TIME, startedAt);
      }
    }
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    long finishedAt = System.currentTimeMillis();
    String id = getStore(context).remove(ID, String.class);
    if (id != null && notificationUrl != null && jobId != null) {
      Map<String, Object> map = new HashMap<>();
      map.put("id", id);
      map.put("testclass", context.getRequiredTestClass().getCanonicalName());
      map.put("testcase", context.getRequiredTestMethod().getName());
      map.put("job_id", jobId);
      long startedAt = getStore(context).remove(START_TIME, long.class);
      map.put("result", "passed");
      map.put("started_at", startedAt);
      map.put("finished_at", finishedAt);

      Optional<Throwable> executionException = context.getExecutionException();

      if (executionException.isPresent()) {
        Throwable ex = executionException.get();
        map.put("result", "failed");
        map.put("exception", ex.getClass().getCanonicalName());
        map.put("message", ex.getMessage());
        map.put("stacktrace", stacktraceToString(ex));
      } else {
        map.put("result", "passed");
      }

      notify(map, notificationUrl + "/" + id);
    }
  }

  private String stacktraceToString(Throwable e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  private String notify(Map<String, Object> map, String notificationUrl) {
    try {
      RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(map));
      Request request = new Request.Builder()
        .url(notificationUrl).post(body).build();

      try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful()) {
          return response.body().string().replace("\"", "");
        } else {
          return null;
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }
}
