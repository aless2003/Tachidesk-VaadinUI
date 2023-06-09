package online.hatsunemiku.tachideskvaadinui.view;


import static org.springframework.http.HttpMethod.GET;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.card.MangaCard;
import online.hatsunemiku.tachideskvaadinui.data.Category;
import online.hatsunemiku.tachideskvaadinui.data.Manga;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
@Route("/")
@CssImport("css/root.css")

public class RootView extends StandardLayout {

  private final RestTemplate client;

  public RootView(RestTemplate client) {
    super("Library");

    this.client = client;
    Settings settings = SerializationUtils.deseralizeSettings();
    var categories = getCategories(settings);
    var manga = getManga(categories, settings);

    Div grid = new Div();
    grid.addClassName("grid");



    for (Manga m : manga) {
      MangaCard card = new MangaCard(settings, m);
      grid.add(card);
    }

    setContent(grid);
  }

  private List<Category> getCategories(Settings settings) {
    String categoryEndpoint = settings.getUrl() + "/api/v1/category";

    ParameterizedTypeReference<List<Category>> typeRef = new ParameterizedTypeReference<>() {};

    List<Category> list = client.exchange(categoryEndpoint, GET, null, typeRef).getBody();

    if (list == null) {
      return new ArrayList<>();
    }

    return list;
  }

  private List<Manga> getManga(List<Category> list, Settings settings) {
    List<Manga> manga = new ArrayList<>();

    for (Category c : list) {
      String template = "%s/api/v1/category/%d";
      String categoryMangaEndpoint = String.format(template, settings.getUrl(), c.getId());

      ParameterizedTypeReference<List<Manga>> typeRef = new ParameterizedTypeReference<>() {};
      List<Manga> mangaList = client.exchange(categoryMangaEndpoint, GET, null, typeRef).getBody();

      if (mangaList == null) {
        continue;
      }

      manga.addAll(mangaList);
    }

    return manga;
  }


}
