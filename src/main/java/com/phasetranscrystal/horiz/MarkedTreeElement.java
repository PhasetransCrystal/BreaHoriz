package com.phasetranscrystal.horiz;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class MarkedTreeElement {

    public final HashSet<IdentEvent<?>> obj = new HashSet<>();
    public final HashMap<ResourceLocation, MarkedTreeElement> ext = new HashMap<>();

    public boolean isEmpty() {
        return obj.isEmpty() && ext.isEmpty();
    }

    public boolean add(IdentEvent<?> ident, ResourceLocation... path) {
        if (path.length == 0) {
            return obj.add(ident);
        } else {
            return ext.computeIfAbsent(path[0], loc -> new MarkedTreeElement()).add(ident, Arrays.copyOfRange(path, 1, path.length));
        }
    }

    public boolean tidyUp() {
        if (isEmpty()) return true;
        ext.values().removeIf(MarkedTreeElement::tidyUp);
        return isEmpty();
    }

    public boolean removeInPath(IdentEvent<?> ident, ResourceLocation... path) {
        if (path.length == 0) {
            return obj.remove(ident);
        } else {
            return Optional.ofNullable(ext.get(path[0]))
                    .map(element -> element.removeInPath(ident, Arrays.copyOfRange(path, 1, path.length)))
                    .orElse(false);
        }
    }

    public boolean remove(IdentEvent<?> ident) {
        boolean flag = obj.remove(ident);
        for (MarkedTreeElement element : ext.values()) {
            flag = element.remove(ident) | flag;
        }
        return flag;
    }

    public boolean remove(Class<?> clazz) {
        boolean flag = obj.removeIf(i -> i.event().equals(clazz));
        for (MarkedTreeElement element : ext.values()) {
            flag = element.remove(clazz) | flag;
        }
        return flag;
    }

    public Collection<IdentEvent<?>> removeSelf(ResourceLocation... path) {
        if (path.length == 0) {
            var collection = List.copyOf(obj);
            obj.clear();
            return collection;
        } else {
            return Optional.ofNullable(ext.get(path[0]))
                    .map(element -> element.removeSelf(Arrays.copyOfRange(path, 1, path.length)))
                    .orElse(Collections.emptyList());
        }
    }

    public Collection<IdentEvent<?>> removeAll(ResourceLocation... path) {
        if (path.length == 0) {
            var collection = new ArrayList<>(obj);
            ext.values().stream().map(MarkedTreeElement::removeAll).forEach(collection::addAll);
            obj.clear();
            ext.clear();
            return collection;
        } else {
            return Optional.ofNullable(ext.remove(path[0]))
                    .map(element -> element.removeAll(Arrays.copyOfRange(path, 1, path.length)))
                    .orElse(Collections.emptyList());
        }
    }

    public boolean contains(ResourceLocation... path) {
        if (path.length == 0) return obj.isEmpty();
        if (path.length == 1) return ext.containsKey(path[0]);
        else
            return Optional.ofNullable(ext.get(path[0])).map(i -> i.contains(Arrays.copyOfRange(path, 1, path.length))).orElse(false);
    }
}
