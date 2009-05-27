package play.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.vfs.VirtualFile;
import play.exceptions.TemplateCompilationException;
import play.exceptions.TemplateNotFoundException;

/**
 * Load templates
 */
public class TemplateLoader {

    protected static Map<String, Template> templates = new HashMap<String, Template>();

    public static Template load(VirtualFile file) {
        String key = (file.relativePath().hashCode()+"").replace("-", "M");
        if (!templates.containsKey(key) || templates.get(key).compiledTemplate == null) {
            Template template = new Template(file.relativePath(), file.contentAsString());
            if(template.loadFromCache()) {
                templates.put(key, template);
            } else {
                templates.put(key, TemplateCompiler.compile(file));
            }
        } else {
            Template template = templates.get(key);
            if (Play.mode == Play.Mode.DEV && template.timestamp < file.lastModified()) {
                templates.put(key, TemplateCompiler.compile(file));
            }
        }
        if (templates.get(key) == null) {
            throw new TemplateNotFoundException(file.relativePath());
        }
        return templates.get(key);
    }

    public static void cleanCompiledCache() {
        // nothing to do in this version
    }

    /**
     * Load a template
     * @param path The path of the template (ex: Application/index.html)
     * @return The executable template
     */
    public static Template load(String path) {
        Template template = null;
        for (VirtualFile vf : Play.templatesPath) {
            if(vf == null) {
                continue;
            }
            VirtualFile tf = vf.child(path);
            if (tf.exists()) {
                template = TemplateLoader.load(tf);
                break;
            }
        }
        //TODO: remove ?
        if (template == null) {
            VirtualFile tf = Play.getVirtualFile(path);
            if (tf != null && tf.exists()) {
                template = TemplateLoader.load(tf);
            } else {
                throw new TemplateNotFoundException(path);
            }
        }
        return template;
    }

    /**
     * List all found templates
     * @return A list of executable templates
     */
    public static List<Template> getAllTemplate() {
        List<Template> res = new ArrayList<Template>();
        for (VirtualFile virtualFile : Play.templatesPath) {
            scan(res, virtualFile);
        }
        return res;
    }

    private static void scan(List<Template> templates, VirtualFile current) {
        if (!current.isDirectory()) {
            long start = System.currentTimeMillis();
            Template template = load(current);
            try {
                template.compile();
                Logger.trace("%sms to load %s", System.currentTimeMillis() - start, current.getName());
            } catch (TemplateCompilationException e) {
                Logger.error("Template %s does not compile at line %d", e.getTemplate().name, e.getLineNumber());
                throw e;
            }
            templates.add(template);
        } else if (!current.getName().startsWith(".")) {
            for (VirtualFile virtualFile : current.list()) {
                scan(templates, virtualFile);
            }
        }
    }
}
