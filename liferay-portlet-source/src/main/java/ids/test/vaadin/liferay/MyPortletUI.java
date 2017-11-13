package ids.test.vaadin.liferay;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;

import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Theme("demotheme")
@SuppressWarnings("serial")
@Widgetset("ids.test.vaadin.liferay.AppWidgetSet")
@Component(service = UI.class, property = {
        "com.liferay.portlet.display-category=category.sample",
        "javax.portlet.name=DemoVaadin",
        "com.liferay.portlet.instanceable=true",
        "javax.portlet.display-name=Demo GUI",
        "javax.portlet.security-role-ref=power-user,user",
        "com.vaadin.osgi.liferay.portlet-ui=true"}, scope = ServiceScope.PROTOTYPE)
public class MyPortletUI extends UI {

    private static Log log = LogFactoryUtil.getLog(MyPortletUI.class);

    private static final String OPEN_API = "https://jsonplaceholder.typicode.com";

    private TabSheet tabSheet;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void init(VaadinRequest request) {

        tabSheet = new TabSheet();

        loadTab1();
        loadTab2();


        tabSheet.setSizeFull();
        tabSheet.setHeight("500px");
        tabSheet.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);

        setContent(tabSheet);
    }

    private void loadTab1() {
        // ************* ALINEAR COMPONENTE DESDE API VAADIN ************* //
        Button btn1 = new Button("Btn1 MIDDLE_LEFT");
        Button btn2 = new Button("Btn2 MIDDLE_RIGHT");

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setCaption("ALINEAR COMPONENTE DESDE API VAADIN");
        horizontalLayout.addStyleName("hl-style"); // la definicion del estilo se encuentra en webapps/vaadin/themes/mytheme/mytheme.scss
        horizontalLayout.setWidth("100%");
        horizontalLayout.addComponents(btn1, btn2);

        horizontalLayout.setExpandRatio(btn1, 0);
        horizontalLayout.setExpandRatio(btn2, 0);

        horizontalLayout.setComponentAlignment(btn1, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(btn2, Alignment.MIDDLE_RIGHT);

        // ************* HACER GET Y MOSTRAR RESULTADO EN VENTANA ************* //

        Button httpGetBtn = new Button("HTTP GET");

        httpGetBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY); // estilos especificos de vaadin
        httpGetBtn.addClickListener(e -> {

            String get = sendGET();

            if (get == null) {

                Notification.show("ERROR AL INVOCAR SERVICIO", Notification.Type.ERROR_MESSAGE);
            } else {

                Label lbl = new Label();
                lbl.setContentMode(ContentMode.HTML);
                lbl.setValue(get);

                Window window = new Window("Resultado");
                window.setModal(true);
                window.setWidth("80%");
                window.setHeight("80%");
                window.setContent(lbl);

                UI.getCurrent().addWindow(window);
            }
        });

        // ************* POST Y MOSTRAR RESULTADO EN VENTANA ************* //

        Button httpPostBtn = new Button("HTTP POST");

        httpPostBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        httpPostBtn.addClickListener(e -> {
            if (sendPOST() == null) {
                Notification.show("ERROR AL INVOCAR SERVICIO", Notification.Type.ERROR_MESSAGE);
            } else {
                Notification.show("ELEMENTO CREADO");
            }
        });


        CssLayout btnContainer = new CssLayout(httpGetBtn, httpPostBtn);

        btnContainer.setCaption("PEDIDOS HTTP");
        btnContainer.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP); // agrupa componentes


        // ************* USO DE ESTILOS ************* //

        // el CssLayout es el mejor componente para crear layouts personalizados y es el más rápido
        CssLayout customCssLayout = new CssLayout();

        customCssLayout.setCaption("ESTILOS PERSONALIZADOS");
        customCssLayout.addStyleName("custom-csslayout");
        customCssLayout.addComponents(new CssLayout(), new CssLayout());

        // ************* LINK EXTERNO ************* //

        Button link = new Button("Ir a google");
        link.addStyleName(ValoTheme.BUTTON_LINK);
        link.addClickListener(e -> getUI().getPage().open("http://www.google.com", "_blank"));


        // ******************************************** //

        VerticalLayout root = new VerticalLayout();

        root.setHeightUndefined();
        root.addComponents(horizontalLayout, btnContainer, customCssLayout, link);

        tabSheet.addTab(root, "Example 1");
    }

    private void loadTab2() {


        // ************* GRILLA CON TEXT RENDERER PARA FORMATEAR FECHAS ************* //

        Grid<User> userGrid = new Grid<>("Grilla, listado de usuarios");

        userGrid.addColumn(User::getId).setCaption("Id");
        userGrid.addColumn(User::getName).setCaption("Name");
        userGrid.addColumn(User::getDescription).setCaption("Description");
        userGrid.addColumn(provider -> provider.getBornsDate().format(formatter), new TextRenderer()).setCaption("Born date");
        userGrid.setItems(generateUsers());
        userGrid.setWidth("100%");


        // ************* EJEMPLO DE COMBOBOX ************* //

        ComboBox<User> userComboBox = new ComboBox<>("Seleccionar usuario");

        userComboBox.setItemCaptionGenerator(e -> String.format("Custom Caption %s", e.getId()));
        userComboBox.setItems(generateUsers());
        userComboBox.addValueChangeListener(e -> Notification.show(String.format("Seleccionado el usuario %s", e.getValue().getId())));
        userComboBox.setWidth("300px");

        VerticalLayout root = new VerticalLayout(userGrid, userComboBox);

        root.setSizeUndefined();
        root.setWidth("100%");

        CssLayout scrollable = new CssLayout(root);
        scrollable.setSizeFull();
        scrollable.addStyleName("setOverflowAuto");

        tabSheet.addTab(scrollable,"Ejemplo 2");
    }

    private String sendGET() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(String.format("%s/posts/1", OPEN_API));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;


            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            if (urlConnection.getResponseCode() == 200) {
                return stringBuilder.toString();
            } else {
                return null;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String sendPOST() {

        StringBuilder stringBuilder = new StringBuilder();
        String body = "{ \"title\": \"testing-post\", \"body\": \"pepito\", \"userId\": 1 }";

        try {

            URL url = new URL(String.format("%s/posts", OPEN_API));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-type", "application/json; charset=UTF-8");

            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(body.getBytes("UTF-8"));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;


            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }


            if (urlConnection.getResponseCode() == 201) {
                return stringBuilder.toString();
            } else {
                return null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


    private List<User> generateUsers() {
        return IntStream.range(0, 10)
                .mapToObj(e -> new User())
                .collect(Collectors.toList());
    }


    private class User {

        private int id = (int) (Math.random() * 100);
        private String name = UUID.randomUUID().toString();
        private String description = "description " + name;
        private LocalDateTime bornsDate = LocalDateTime.now();

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getBornsDate() {
            return bornsDate;
        }
    }


    private String getPortletContextName(VaadinRequest request) {
        WrappedPortletSession wrappedPortletSession = (WrappedPortletSession) request
                .getWrappedSession();
        PortletSession portletSession = wrappedPortletSession
                .getPortletSession();

        final PortletContext context = portletSession.getPortletContext();
        final String portletContextName = context.getPortletContextName();
        return portletContextName;
    }

    private Integer getPortalCountOfRegisteredUsers() {
        Integer result = null;

        try {
            result = UserLocalServiceUtil.getUsersCount();
        } catch (SystemException e) {
            log.error(e);
        }

        return result;
    }
}
