package com.groupon.jenkins.extensions;

import com.google.common.primitives.Primitives;
import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import jenkins.model.Jenkins;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.lang.Klass;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DescribableHelper {

    private static final Logger LOG = Logger.getLogger(DescribableHelper.class.getName());

    public static <T> T instantiate(Class<? extends T> clazz, Map<String,?> arguments) throws Exception {
        String[] names = loadConstructorParamNames(clazz);
        Constructor<T> c = findConstructor(clazz, names.length);
        Object[] args = buildArguments(clazz, arguments, c.getGenericParameterTypes(), names, true);
        T o = c.newInstance(args);
        injectSetters(o, arguments);
        return o;
    }


    public static Schema schemaFor(Class<?> clazz) {
        return new Schema(clazz);
    }

    private static Schema schemaFor(Class<?> clazz, Stack<String> tracker) {
        return new Schema(clazz, tracker);
    }

    public static final class Schema {

        private final Class<?> type;
        private final Map<String,ParameterType> parameters;
        private final List<String> mandatoryParameters;

        Schema(Class<?> clazz) {
            this(clazz, new Stack<String>());
        }

        Schema(Class<?> clazz, @Nonnull Stack<String> tracker) {
            this.type = clazz;
            /*if(tracker == null){
                tracker = new Stack<String>();
            }*/
            mandatoryParameters = new ArrayList<String>();
            parameters = new TreeMap<String,ParameterType>();
            String[] names = loadConstructorParamNames(clazz);
            Type[] types = findConstructor(clazz, names.length).getGenericParameterTypes();
            for (int i = 0; i < names.length; i++) {
                mandatoryParameters.add(names[i]);
                parameters.put(names[i], ParameterType.of(types[i], tracker));
            }
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(DataBoundSetter.class)) {
                        f.setAccessible(true);
                        parameters.put(f.getName(), ParameterType.of(f.getGenericType(), tracker));
                    }
                }
                for (Method m : c.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(DataBoundSetter.class)) {
                        Type[] parameterTypes = m.getGenericParameterTypes();
                        if (!m.getName().startsWith("set") || parameterTypes.length != 1) {
                            throw new IllegalStateException(m + " cannot be a @DataBoundSetter");
                        }
                        m.setAccessible(true);
                        parameters.put(Introspector.decapitalize(m.getName().substring(3)), ParameterType.of(m.getGenericParameterTypes()[0], tracker));
                    }
                }
            }
        }

        /**
         * A concrete class, usually {@link Describable}.
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * A map from parameter names to types.
         * A parameter name is either the name of an argument to a {@link DataBoundConstructor},
         * or the JavaBeans property name corresponding to a {@link DataBoundSetter}.
         */
        public Map<String,ParameterType> parameters() {
            return parameters;
        }

        /**
         * Mandatory (constructor) parameters, in order.
         * Parameters at the end of the list may be omitted, in which case they are assumed to be null or some other default value
         * (in these cases it would be better to use {@link DataBoundSetter} on the type definition).
         * Will be keys in {@link #parameters}.
         */
        public List<String> mandatoryParameters() {
            return mandatoryParameters;
        }

        /**
         * Corresponds to {@link Descriptor#getDisplayName} where available.
         */
        public String getDisplayName() {
            for (Descriptor<?> d : getDescriptorList()) {
                if (d.clazz == type) {
                    return d.getDisplayName();
                }
            }
            return type.getSimpleName();
        }

        /**
         * Loads help defined for this object as a whole or one of its parameters.
         * Note that you may need to use {@link Util#replaceMacro(String, Map)}
         * to replace {@code ${rootURL}} with some other value.
         * @param parameter if specified, one of {@link #parameters}; else for the whole object
         * @return some HTML (in English locale), if available, else null
         * @see Descriptor#doHelp
         */
        public @CheckForNull String getHelp(@CheckForNull String parameter) throws IOException {
            for (Klass<?> c = Klass.java(type); c != null; c = c.getSuperClass()) {
                URL u = c.getResource(parameter == null ? "help.html" : "help-" + parameter + ".html");
                if (u != null) {
                    return IOUtils.toString(u, "UTF-8");
                }
            }
            return null;
        }

        @Override public String toString() {
            StringBuilder b = new StringBuilder("(");
            boolean first = true;
            Map<String,ParameterType> params = new TreeMap<String,ParameterType>(parameters());
            for (String param : mandatoryParameters()) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                b.append(param).append(": ").append(params.remove(param));
            }
            for (Map.Entry<String,ParameterType> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                b.append('[').append(entry.getKey()).append(": ").append(entry.getValue()).append(']');
            }
            return b.append(')').toString();
        }

    }

    /**
     * A type of a parameter to a class.
     */
    public static abstract class ParameterType {
        @Nonnull
        private final Type actualType;

        public Type getActualType() {
            return actualType;
        }

        ParameterType(Type actualType) {
            this.actualType = actualType;
        }

        static ParameterType of(Type type){
            return of(type, new Stack<String>());
        }

        private static ParameterType of(Type type, @Nonnull Stack<String> tracker) {
            try {
                if (type instanceof Class) {
                    Class<?> c = (Class<?>) type;
                    if (c == String.class || Primitives.unwrap(c).isPrimitive()) {
                        return new AtomicType(c);
                    }
                    if (Enum.class.isAssignableFrom(c)) {
                        List<String> constants = new ArrayList<String>();
                        for (Enum<?> value : c.asSubclass(Enum.class).getEnumConstants()) {
                            constants.add(value.name());
                        }
                        return new EnumType(c, constants.toArray(new String[constants.size()]));
                    }
                    if (c == URL.class) {
                        return new AtomicType(String.class);
                    }
                    if (c.isArray()) {
                        return new ArrayType(c);
                    }
                    // Assume it is a nested object of some sort.
                    Set<Class<?>> subtypes = findSubtypes(c);
                    if ((subtypes.isEmpty() && !Modifier.isAbstract(c.getModifiers())) || subtypes.equals(Collections.singleton(c))) {
                        // Probably homogeneous. (Might be concrete but subclassable.)
                        return new HomogeneousObjectType(c);
                    } else {
                        // Definitely heterogeneous.
                        Map<String,List<Class<?>>> subtypesBySimpleName = new HashMap<String,List<Class<?>>>();
                        for (Class<?> subtype : subtypes) {
                            String simpleName = subtype.getSimpleName();
                            List<Class<?>> bySimpleName = subtypesBySimpleName.get(simpleName);
                            if (bySimpleName == null) {
                                subtypesBySimpleName.put(simpleName, bySimpleName = new ArrayList<Class<?>>());
                            }
                            bySimpleName.add(subtype);
                        }
                        Map<String,Schema> types = new TreeMap<String,Schema>();
                        for (Map.Entry<String,List<Class<?>>> entry : subtypesBySimpleName.entrySet()) {
                            if (entry.getValue().size() == 1) { // normal case: unambiguous via simple name
                                try {
                                    String key = entry.getKey();
                                    if(tracker.search(key) < 0) {
                                        tracker.push(key);
                                        types.put(key, schemaFor(entry.getValue().get(0), tracker));
                                        tracker.pop();
                                    }
                                } catch (Exception x) {
                                    LOG.log(Level.FINE, "skipping subtype", x);
                                }
                            } else { // have to diambiguate via FQN
                                for (Class<?> subtype : entry.getValue()) {
                                    try {
                                        String name = subtype.getName();
                                        if(tracker.search(name) < 0) {
                                            tracker.push(name);
                                            types.put(name, schemaFor(subtype, tracker));
                                            tracker.pop();
                                        }
                                    } catch (Exception x) {
                                        LOG.log(Level.FINE, "skipping subtype", x);
                                    }
                                }
                            }
                        }
                        return new HeterogeneousObjectType(c, types);
                    }
                }
                if (acceptsList(type)) {
                    return new ArrayType(type, of(((ParameterizedType) type).getActualTypeArguments()[0]));
                }
                throw new UnsupportedOperationException("do not know how to categorize attributes of type " + type);
            } catch (Exception x) {
                return new ErrorType(x, type);
            }
        }
    }

    public static final class AtomicType extends ParameterType {
        AtomicType(Class<?> clazz) {
            super(clazz);
        }

        public Class<?> getType() {
            return (Class) getActualType();
        }

        @Override public String toString() {
            return Primitives.unwrap((Class)getActualType()).getSimpleName();
        }
    }

    public static final class EnumType extends ParameterType {
        private final String[] values;
        EnumType(Class<?> clazz, String[] values) {
            super(clazz);
            this.values = values;
        }

        public Class<?> getType() {
            return (Class) getActualType();
        }

        /**
         * A list of enumeration values.
         */
        public String[] getValues() {
            return values.clone();
        }
        @Override public String toString() {
            return ((Class)getActualType()).getSimpleName() + Arrays.toString(values);
        }
    }

    public static final class ArrayType extends ParameterType {
        private final ParameterType elementType;
        ArrayType(Class<?> actualClass) {
            this(actualClass, of(actualClass.getComponentType()));
        }

        ArrayType(Type actualClass, ParameterType elementType) {
            super(actualClass);
            this.elementType = elementType;
        }

        /**
         * The element type of the array or list.
         */
        public ParameterType getElementType() {
            return elementType;
        }
        @Override public String toString() {
            return elementType + "[]";
        }
    }

    public static final class HomogeneousObjectType extends ParameterType {
        private final Schema type;
        HomogeneousObjectType(Class<?> actualClass) {
            super(actualClass);
            this.type = schemaFor(actualClass);
        }

        public Class<?> getType() {
            return (Class) getActualType();
        }

        /**
         * The schema representing a type of nested object.
         */
        public Schema getSchemaType() {
            return type;
        }

        /**
         * The actual class underlying the type.
         */
        @Override public String toString() {
            return type.getType().getSimpleName() + type;
        }
    }

    /**
     * A parameter (or array element) which could take any of the indicated concrete object types.
     */
    public static final class HeterogeneousObjectType extends ParameterType {
        private final Map<String,Schema> types;
        HeterogeneousObjectType(Class<?> supertype, Map<String,Schema> types) {
            super(supertype);
            this.types = types;
        }

        public Class<?> getType() {
            return (Class) getActualType();
        }

        /**
         * A map from names which could be passed to {@link #CLAZZ} to types of allowable nested objects.
         */
        public Map<String,Schema> getTypes() {
            return types;
        }
        @Override public String toString() {
            return getType().getSimpleName() + types;
        }
    }

    public static final class ErrorType extends ParameterType {
        private final Exception error;
        ErrorType(Exception error, Type type) {
            super(type);
            LOG.log(Level.FINE, null, error);
            this.error = error;
        }
        public Exception getError() {
            return error;
        }
        @Override public String toString() {
            return error.toString();
        }
    }



    public static final String CLAZZ = "$class";

    private static Object[] buildArguments(Class<?> clazz, Map<String,?> arguments, Type[] types, String[] names, boolean callEvenIfNoArgs) throws Exception {
        Object[] args = new Object[names.length];
        boolean hasArg = callEvenIfNoArgs;
        for (int i = 0; i < args.length; i++) {
            String name = names[i];
            hasArg |= arguments.containsKey(name);
            Object a = arguments.get(name);
            Type type = types[i];
            if (a != null) {
                args[i] = coerce(clazz.getName() + "." + name, type, a);
            } else if (type == boolean.class) {
                args[i] = false;
            } else if (type instanceof Class && ((Class) type).isPrimitive() && callEvenIfNoArgs) {
                throw new UnsupportedOperationException("not yet handling @DataBoundConstructor default value of " + type + "; pass an explicit value for " + name);
            } else {
                // TODO this might be fine (ExecutorStep.label), or not (GenericSCMStep.scm); should inspect parameter annotations for @Nonnull and throw an UOE if found
            }
        }
        return hasArg ? args : null;
    }

    @SuppressWarnings("unchecked")
    private static Object coerce(String context, Type type, @Nonnull Object o) throws Exception {
        if (type instanceof Class) {
            o = ReflectionCache.getCachedClass((Class) type).coerceArgument(o);
        }
        if (type instanceof Class && Primitives.wrap((Class) type).isInstance(o)) {
            return o;
        } else if (o instanceof Map) {
            Map<String,Object> m = new HashMap<String,Object>();
            for (Map.Entry<?,?> entry : ((Map<?,?>) o).entrySet()) {
                m.put((String) entry.getKey(), entry.getValue());
            }

            String clazzS = (String) m.remove(CLAZZ);
            Class<?> clazz;
            if (clazzS == null) {
                if (Modifier.isAbstract(((Class) type).getModifiers())) {
                    throw new UnsupportedOperationException("must specify " + CLAZZ + " with an implementation of " + type);
                }
                clazz = (Class) type;
            } else if (clazzS.contains(".")) {
                Jenkins j = Jenkins.getInstance();
                ClassLoader loader = j != null ? j.getPluginManager().uberClassLoader : DescribableHelper.class.getClassLoader();
                clazz = loader.loadClass(clazzS);
            } else if (type instanceof Class) {
                clazz = null;
                for (Class<?> c : findSubtypes((Class<?>) type)) {
                    if (c.getSimpleName().equals(clazzS)) {
                        if (clazz != null) {
                            throw new UnsupportedOperationException(clazzS + " as a " + type +  " could mean either " + clazz.getName() + " or " + c.getName());
                        }
                        clazz = c;
                    }
                }
                if (clazz == null) {
                    throw new UnsupportedOperationException("no known implementation of " + type + " is named " + clazzS);
                }
            } else {
                throw new UnsupportedOperationException("JENKINS-26535: do not know how to handle " + type);
            }
            return instantiate(clazz.asSubclass((Class) type), m);
        } else if (o instanceof String && type instanceof Class && ((Class) type).isEnum()) {
            return Enum.valueOf(((Class) type).asSubclass(Enum.class), (String) o);
        } else if (o instanceof String && type == URL.class) {
            return new URL((String) o);
        } else if (o instanceof String && (type == char.class || type == Character.class) && ((String) o).length() == 1) {
            return ((String) o).charAt(0);
        } else if (o instanceof List && type instanceof Class && ((Class) type).isArray()) {
            Class<?> componentType = ((Class) type).getComponentType();
            List<Object> list = mapList(context, componentType, (List) o);
            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        } else if (o instanceof List && acceptsList(type)) {
            return mapList(context, ((ParameterizedType) type).getActualTypeArguments()[0], (List) o);
        } else {
            throw new ClassCastException(context + " expects " + type + " but received " + o.getClass());
        }
    }

    /** Whether this type is generic of {@link List} or a supertype thereof (such as {@link Collection}). */
    @SuppressWarnings("unchecked")
    private static boolean acceptsList(Type type) {
        return type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class && ((Class) ((ParameterizedType) type).getRawType()).isAssignableFrom(List.class);
    }

    private static List<Object> mapList(String context, Type type, List<?> list) throws Exception {
        List<Object> r = new ArrayList<Object>();
        for (Object elt : list) {
            r.add(coerce(context, type, elt));
        }
        return r;
    }

    private static String[] loadConstructorParamNames(Class<?> clazz) {
        return new ClassDescriptor(clazz).loadConstructorParamNames();
    }

    // adapted from RequestImpl
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> findConstructor(Class<? extends T> clazz, int length) {
        Constructor<T>[] ctrs = (Constructor<T>[]) clazz.getConstructors();
        for (Constructor<T> c : ctrs) {
            if (c.getAnnotation(DataBoundConstructor.class) != null) {
                if (c.getParameterTypes().length != length) {
                    throw new IllegalArgumentException(c + " has @DataBoundConstructor but it doesn't match with your .stapler file. Try clean rebuild");
                }
                return c;
            }
        }
        for (Constructor<T> c : ctrs) {
            if (c.getParameterTypes().length == length) {
                return c;
            }
        }
        throw new IllegalArgumentException(clazz + " does not have a constructor with " + length + " arguments");
    }

    /**
     * Injects via {@link DataBoundSetter}
     */
    private static void injectSetters(Object o, Map<String,?> arguments) throws Exception {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(DataBoundSetter.class)) {
                    f.setAccessible(true);
                    if (arguments.containsKey(f.getName())) {
                        Object v = arguments.get(f.getName());
                        f.set(o, v != null ? coerce(c.getName() + "." + f.getName(), f.getType(), v) : null);
                    }
                }
            }
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(DataBoundSetter.class)) {
                    Type[] parameterTypes = m.getGenericParameterTypes();
                    if (!m.getName().startsWith("set") || parameterTypes.length != 1) {
                        throw new IllegalStateException(m + " cannot be a @DataBoundSetter");
                    }
                    m.setAccessible(true);
                    Object[] args = buildArguments(c, arguments, parameterTypes, new String[] {Introspector.decapitalize(m.getName().substring(3))}, false);
                    if (args != null) {
                        m.invoke(o, args);
                    }
                }
            }
        }
    }





    static Set<Class<?>> findSubtypes(Class<?> supertype) {
        Set<Class<?>> clazzes = new HashSet<Class<?>>();
        for (Descriptor<?> d : getDescriptorList()) {
            if (supertype.isAssignableFrom(d.clazz)) {
                clazzes.add(d.clazz);
            }
        }
        if (supertype == ParameterValue.class) { // TODO JENKINS-26093 hack, pending core change
            for (Class<?> d : findSubtypes(ParameterDefinition.class)) {
                String name = d.getName();
                if (name.endsWith("Definition")) {
                    try {
                        Class<?> c = d.getClassLoader().loadClass(name.replaceFirst("Definition$", "Value"));
                        if (supertype.isAssignableFrom(c)) {
                            clazzes.add(c);
                        }
                    } catch (ClassNotFoundException x) {
                        // ignore
                    }
                }
            }
        }
        return clazzes;
    }

    @SuppressWarnings("rawtypes")
    private static List<? extends Descriptor> getDescriptorList() {
        Jenkins j = Jenkins.getInstance();
        if (j != null) {
            // Jenkins.getDescriptorList does not work well since it is limited to descriptors declaring one supertype, and does not work at all for SimpleBuildStep.
            return j.getExtensionList(Descriptor.class);
        } else {
            // TODO should be part of ExtensionList.lookup in core, but here now for benefit of tests:
            List<Descriptor<?>> descriptors = new ArrayList<Descriptor<?>>();
            for (IndexItem<Extension,Object> item : Index.load(Extension.class, Object.class)) {
                try {
                    Object o = item.instance();
                    if (o instanceof Descriptor) {
                        descriptors.add((Descriptor) o);
                    }
                } catch (InstantiationException x) {
                    // ignore for now
                }
            }
            return descriptors;
        }
    }

    private DescribableHelper() {}

}
