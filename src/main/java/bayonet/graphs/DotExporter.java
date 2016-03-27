package bayonet.graphs;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import briefj.BriefIO;



public class DotExporter<V,E>
{
  public DotExporter(Graph<V, E> graph)
  {
    this.graph = graph;
  }
  
  private final Graph<V, E> graph;
  public VertexNameProvider<V> vertexNameProvider = node -> node.toString();
  public EdgeNameProvider<E> edgeNameProvide = null;
  private LinkedHashMap<String,Function<V, String>> vertexAttributeProviders = new LinkedHashMap<>();
  private LinkedHashMap<String,Function<E, String>> edgeAttributeProviders = new LinkedHashMap<>();
  public void addVertexAttribute(String attributeName, Function<V, String> attributeProvider)
  {
    vertexAttributeProviders.put(attributeName, attributeProvider);
  }
  public void addEdgeAttribute(String attributeName, Function<E, String> attributeProvider)
  {
    edgeAttributeProviders.put(attributeName, attributeProvider);
  }
  private static class AttributeProvideAdaptor<T> implements ComponentAttributeProvider<T>
  {
    private final LinkedHashMap<String,Function<T, String>> attributeProviders;
    
    private AttributeProvideAdaptor(
        LinkedHashMap<String, Function<T, String>> attributeProviders)
    {
      this.attributeProviders = attributeProviders;
    }
    @Override
    public Map<String, String> getComponentAttributes(T component)
    {
      LinkedHashMap<String,String> result = new LinkedHashMap<>();
      for (String key : attributeProviders.keySet())
        result.put(key, attributeProviders.get(key).apply(component));
      return result;
    }
  }
  public void export(File f)
  {
    DOTExporter<V,E> exporter = new DOTExporter<>(
        new IntegerNameProvider<>(),
        vertexNameProvider,
        edgeNameProvide,
        new DotExporter.AttributeProvideAdaptor<>(vertexAttributeProviders),
        new DotExporter.AttributeProvideAdaptor<>(edgeAttributeProviders)
        );
    PrintWriter output = BriefIO.output(f);
    exporter.export(output, graph);
    output.close();
  }
}