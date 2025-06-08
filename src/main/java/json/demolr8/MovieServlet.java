package json.demolr8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/movies")
public class MovieServlet extends HttpServlet {
    private ObjectMapper mapper;
    private static final String FILE_PATH = System.getProperty("user.home") + File.separator + "movies.json";

    @Override
    public void init() throws ServletException {
        mapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<JsonNode> movies = readMoviesFromFile();
            resp.getWriter().print(mapper.writeValueAsString(movies));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при чтении: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            JsonNode newMovie = mapper.readTree(req.getReader());
            List<JsonNode> movies = readMoviesFromFile();
            movies.add(newMovie);
            writeMoviesToFile(movies);

            ObjectNode response = mapper.createObjectNode();
            response.put("status", "success");
            response.put("message", "Фильм успешно добавлен");

            resp.getWriter().print(mapper.writeValueAsString(response));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при сохранении: " + e.getMessage());
        }
    }

    private List<JsonNode> readMoviesFromFile() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<List<JsonNode>>() {});
    }

    private void writeMoviesToFile(List<JsonNode> movies) throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, movies);
    }

    private void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        ObjectNode error = mapper.createObjectNode();
        error.put("status", "error");
        error.put("message", message);
        resp.getWriter().print(mapper.writeValueAsString(error));
    }
}
