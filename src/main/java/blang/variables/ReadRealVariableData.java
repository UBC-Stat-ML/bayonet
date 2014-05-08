package blang.variables;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import briefj.BriefIO;

public class ReadRealVariableData {

	private Map<Integer,RealVariable> data = new HashMap<Integer,RealVariable>();

	public ReadRealVariableData(File file) {
		int i = 0;
		for (List<String> datum : BriefIO.readLines(file).splitCSV())
		{
			double parsedDatum = Double.parseDouble(datum.get(0));
			RealVariable dataum = new RealVariable(parsedDatum);
			data.put(i++, dataum);
		}
	}

	public Map<Integer,RealVariable> getData()
	{
		return data;
	}
}
