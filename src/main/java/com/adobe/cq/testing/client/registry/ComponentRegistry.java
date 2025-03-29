package com.adobe.cq.testing.client.registry;

import com.adobe.cq.testing.client.components.AbstractComponent;
import com.adobe.cq.testing.client.components.collab.Ratings;
import com.adobe.cq.testing.client.components.commerce.ShoppingCart;
import com.adobe.cq.testing.client.components.foundation.*;
import com.adobe.cq.testing.client.components.foundation.Text;
import com.adobe.cq.testing.client.components.foundation.form.*;
import com.adobe.cq.testing.client.components.foundation.parsys.ColCtrl;
import com.adobe.cq.testing.client.components.tagging.TagCloud;

import java.util.HashMap;
import java.util.Map;

public class ComponentRegistry {
    private static Map<String, Class<? extends AbstractComponent>> components = new HashMap<>();
    // registers all known component wrappers
    private ComponentRegistry() {
        // Prevent instantiation
    }
    static {
        // foundation components
        components.put(Carousel.RESOURCE_TYPE, Carousel.class);
        components.put(Chart.RESOURCE_TYPE, Chart.class);
        components.put(ColCtrl.RESOURCE_TYPE, ColCtrl.class);
        components.put(Download.RESOURCE_TYPE, Download.class);
        components.put(External.RESOURCE_TYPE, External.class);
        components.put(Flash.RESOURCE_TYPE, Flash.class);
        components.put(Image.RESOURCE_TYPE, Image.class);
        components.put(List.RESOURCE_TYPE, List.class);
        components.put(Reference.RESOURCE_TYPE, Reference.class);
        components.put(Search.RESOURCE_TYPE, Search.class);
        components.put(Sitemap.RESOURCE_TYPE, Sitemap.class);
        components.put(Slideshow.RESOURCE_TYPE, Slideshow.class);
        components.put(Table.RESOURCE_TYPE, Table.class);
        components.put(Text.RESOURCE_TYPE, Text.class);
        components.put(TextImage.RESOURCE_TYPE, TextImage.class);
        components.put(Title.RESOURCE_TYPE, Title.class);
        components.put(TagCloud.RESOURCE_TYPE, TagCloud.class);
        components.put(ParSys.RESOURCE_TYPE, ParSys.class);

        // form components
        components.put(Start.RESOURCE_TYPE, Start.class);
        components.put(End.RESOURCE_TYPE, End.class);
        components.put(com.adobe.cq.testing.client.components.foundation.form.Text.RESOURCE_TYPE,
                com.adobe.cq.testing.client.components.foundation.form.Text.class);
        components.put(Address.RESOURCE_TYPE, Address.class);
        components.put(Captcha.RESOURCE_TYPE, Captcha.class);
        components.put(Checkbox.RESOURCE_TYPE, Checkbox.class);
        components.put(Dropdown.RESOURCE_TYPE, Dropdown.class);
        components.put(FileUpload.RESOURCE_TYPE, FileUpload.class);
        components.put(ImageUpload.RESOURCE_TYPE, ImageUpload.class);
        components.put(Hidden.RESOURCE_TYPE, Hidden.class);
        components.put(ImageButton.RESOURCE_TYPE, ImageButton.class);
        components.put(Password.RESOURCE_TYPE, Password.class);
        components.put(RadioGroup.RESOURCE_TYPE, RadioGroup.class);

        // collab components
        components.put(Ratings.RESOURCE_TYPE, Ratings.class);

        // commerce components
        components.put(Address.RESOURCE_TYPE, Address.class);
        components.put(ShoppingCart.RESOURCE_TYPE, ShoppingCart.class);
    }

    public static void registerComponent(String resourceType, Class<? extends AbstractComponent> clazz) {
        components.put(resourceType, clazz);
    }

    public static Class<? extends AbstractComponent> getComponentClass(String resourceType) {
        return components.get(resourceType);
    }

    public static boolean isComponentRegistered(String resourceType) {
        return components.containsKey(resourceType);
    }
}
