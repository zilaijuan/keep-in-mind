import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
* 这是一个CompletableFuture的小应用类。放在这里记录一下，用于自己学习CompletableFuture.
* 这个类获得于某外资银行面试，如有侵权请联系我删除。
**/

public class FutureQuestion {

    // You have a process gets a request, does some calculations, writes the results to a database
    // and then commits the data.
    // You have access to a client that performs the calculation (CalculationClient)
    // and another that writes to the database (DatabaseClient)

    public static void main(String[] args) throws Exception {
        CalcuationRequest calcRequest = generateCalculationRequest(101);
        DatabaseClient dbClient = new DatabaseClient();
        CalculationClient calcClient = new CalculationClient();
        try {
            processRequest(dbClient, calcClient, calcRequest);
        } finally {
            dbClient.scheduler.shutdown();
            calcClient.scheduler.shutdown();
        }
        System.out.println("Finished");
    }
    public static void processRequest(DatabaseClient dbClient, CalculationClient calcClient, CalcuationRequest calcRequest) throws ExecutionException, InterruptedException {
        
    }
    
    public static class DatabaseClient {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        public CompletableFuture<OperationReport> startTransaction() {
            Random random = new Random();
            int delay = 1 + random.nextInt(10);
            boolean isFailure = random.nextInt(100) > 96;
            CompletableFuture<OperationReport> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                if(isFailure){
                    future.completeExceptionally(new Exception("Failed because you were unlucky!"));
                } else {
                    String transactionId = UUID.randomUUID().toString();
                    System.out.println("Opened transaction: " + transactionId);
                    future.complete(new OperationReport(transactionId));
                }
            }, delay, TimeUnit.SECONDS);
            return future;
        }
        public CompletableFuture<OperationReport> writeToDatabase(SubCalculationResult result, String transactionId) {
            Random random = new Random();
            int delay = 1 + random.nextInt(10);
            boolean isFailure = random.nextInt(100) > 96;
            CompletableFuture<OperationReport> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                if(isFailure){
                    future.completeExceptionally(new Exception("Failed because you were unlucky!"));
                } else {
                    System.out.println("Wrote result " + result.getResultId() + " to transaction " + transactionId);
                    future.complete(new OperationReport(transactionId));
                }
            }, delay, TimeUnit.SECONDS);
            return future;
        }
        public CompletableFuture<OperationReport>  commitTransaction(String transactionId){
            Random random = new Random();
            int delay = 1 + random.nextInt(10);
            boolean isFailure = random.nextInt(100) > 96;
            CompletableFuture<OperationReport> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                if(isFailure){
                    future.completeExceptionally(new Exception("Failed because you were unlucky!"));
                } else {
                    future.complete(new OperationReport(transactionId));
                }
            }, delay, TimeUnit.SECONDS);
            return future;
        }
    }

    public static class OperationReport {
        private boolean isSuccessful;
        private String error;
        private String transactionId;
        public OperationReport(String transactionId) {
            this.transactionId = transactionId;
        }
        public String getTransactionId(){
            return transactionId;
        }
    }
    public static class CalculationClient {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        public CompletableFuture<SubCalculationResult> calculate(SubCalculationRequest request) {
            Random random = new Random();
            int delay = 1 + random.nextInt(10);
            CompletableFuture<SubCalculationResult> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                future.complete(fromSubCalculationRequest(request));
            }, delay, TimeUnit.SECONDS);
            return future;
        }
    }
    public static class CalcuationRequest {
        private String requestId;
        private List<SubCalculationRequest> calculationRequests;
        public CalcuationRequest(String requestId, List<SubCalculationRequest> calculationRequests) {
            this.requestId = requestId;
            this.calculationRequests = calculationRequests;
        }
        public String getRequestId() {
            return requestId;
        }
        public List<SubCalculationRequest> getCalculationRequests() {
            return calculationRequests;
        }
    }
    public static class SubCalculationRequest {
        private String subRequestId;
        private String calculationType;
        private String calculationData;
        public SubCalculationRequest(String subRequestId, String calculationType, String calculationData) {
            this.subRequestId = subRequestId;
            this.calculationType = calculationType;
            this.calculationData = calculationData;
        }
        // Getters and Setters
        public String getSubRequestId() {
            return subRequestId;
        }
        public String getCalculationType() {
            return calculationType;
        }
        public String getCalculationData() {
            return calculationData;
        }
    }
    public static class SubCalculationResult {
        private String requestId;
        private String resultId;
        private String resultData;
        public SubCalculationResult(String requestId, String resultId, String resultData) {
            this.requestId = requestId;
            this.resultId = resultId;
            this.resultData = resultData;
        }
        // Getters and Setters
        public String getRequestId() {
            return requestId;
        }
        public String getResultId() {
            return resultId;
        }
        public String getResultData() {
            return resultData;
        }
    }
    private static CalcuationRequest generateCalculationRequest(int numOfSubRequests){
        List<SubCalculationRequest> subCalculationRequests = IntStream.range(1,numOfSubRequests).mapToObj(i -> new SubCalculationRequest("subRequestId"+i,
                "calculationType"+i, "calculationData" + i)).collect(Collectors.toList());
        return new CalcuationRequest("requestId", subCalculationRequests);
    }
    private static SubCalculationResult fromSubCalculationRequest(SubCalculationRequest subCalculationRequest){
        return new SubCalculationResult(subCalculationRequest.getSubRequestId(), subCalculationRequest.getSubRequestId(), subCalculationRequest.getCalculationData());
    }

}
