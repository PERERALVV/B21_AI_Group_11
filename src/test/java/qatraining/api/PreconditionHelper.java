package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreconditionHelper {

    private static final Logger logger = LoggerFactory.getLogger(PreconditionHelper.class);
    private static boolean seeded = false;

    public static synchronized void ensurePreconditionsExist(String token) {
        if (seeded) {
            return;
        }

        logger.info("[PreconditionHelper] Fetching Admin token for database operations...");
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");

        logger.info("[PreconditionHelper] Wiping existing sales records to guarantee predictable state...");

        // 1. Delete all sales
        Response salesResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/api/sales");
        if (salesResponse.getStatusCode() == 200) {
            List<Map<String, ?>> salesList = salesResponse.jsonPath().getList("");
            if (salesList != null) {
                for (Map<String, ?> sale : salesList) {
                    Number saleId = (Number) sale.get("id");
                    if (saleId != null) {
                        SerenityRest.given()
                                .header("Authorization", "Bearer " + adminToken)
                                .delete("/api/sales/" + saleId.longValue());
                    }
                }
            }
        }

        logger.info("[PreconditionHelper] Seeding/Resetting database categories and plants...");

        // 2. Ensure main category "Flowers" exists
        Long flowersId = null;
        Response mainCatResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/api/categories/main");

        if (mainCatResponse.getStatusCode() == 200) {
            List<Map<String, ?>> mainCats = mainCatResponse.jsonPath().getList("");
            if (mainCats != null) {
                for (Map<String, ?> cat : mainCats) {
                    if ("Flowers".equalsIgnoreCase((String) cat.get("name"))) {
                        flowersId = ((Number) cat.get("id")).longValue();
                        break;
                    }
                }
            }
        }

        if (flowersId == null) {
            Map<String, Object> catBody = new HashMap<>();
            catBody.put("name", "Flowers");
            Response createResponse = SerenityRest.given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(catBody)
                    .post("/api/categories");
            if (createResponse.getStatusCode() == 201) {
                flowersId = createResponse.jsonPath().getLong("id");
            } else {
                flowersId = 1L; // fallback
            }
        }

        // 3. Ensure subcategory "Roses" exists under "Flowers"
        Long rosesId = null;
        Response subCatResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/api/categories/" + flowersId);

        if (subCatResponse.getStatusCode() == 200) {
            List<Map<String, ?>> subs = subCatResponse.jsonPath().getList("subCategories");
            if (subs != null) {
                for (Map<String, ?> sub : subs) {
                    if ("Roses".equalsIgnoreCase((String) sub.get("name"))) {
                        rosesId = ((Number) sub.get("id")).longValue();
                        break;
                    }
                }
            }
        }

        if (rosesId == null) {
            Map<String, Object> parent = new HashMap<>();
            parent.put("id", flowersId);
            Map<String, Object> catBody = new HashMap<>();
            catBody.put("name", "Roses");
            catBody.put("parent", parent);
            Response createResponse = SerenityRest.given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(catBody)
                    .post("/api/categories");
            if (createResponse.getStatusCode() == 201) {
                rosesId = createResponse.jsonPath().getLong("id");
            } else {
                rosesId = 2L; // fallback
            }
        }

        // 4. Create or reset the 5 rose plants
        createOrResetPlant(adminToken, "Red Rose", 15.0, 100, rosesId);
        createOrResetPlant(adminToken, "White Rose", 20.0, 80, rosesId);
        createOrResetPlant(adminToken, "Yellow Rose", 18.0, 90, rosesId);
        createOrResetPlant(adminToken, "Pink Rose", 22.0, 70, rosesId);
        createOrResetPlant(adminToken, "Blue Rose", 25.0, 60, rosesId);

        seeded = true;
        logger.info("[PreconditionHelper] Database seeding/reset complete.");
    }

    private static void createOrResetPlant(String token, String name, double price, int quantity, Long categoryId) {
        Response plantsResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .get("/api/plants");
        
        Long existingId = null;
        if (plantsResponse.getStatusCode() == 200) {
            List<Map<String, ?>> plants = plantsResponse.jsonPath().getList("");
            if (plants != null) {
                for (Map<String, ?> p : plants) {
                    if (name.equalsIgnoreCase((String) p.get("name"))) {
                        existingId = ((Number) p.get("id")).longValue();
                        break;
                    }
                }
            }
        }

        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", categoryId);

        Map<String, Object> plantBody = new HashMap<>();
        plantBody.put("name", name);
        plantBody.put("price", price);
        plantBody.put("quantity", quantity);

        if (existingId != null) {
            plantBody.put("category", categoryMap);
            SerenityRest.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(plantBody)
                    .put("/api/plants/" + existingId);
        } else {
            SerenityRest.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(plantBody)
                    .post("/api/plants/category/" + categoryId);
        }
    }
}
