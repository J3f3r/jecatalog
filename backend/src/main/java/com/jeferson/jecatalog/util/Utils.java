package com.jeferson.jecatalog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jeferson.jecatalog.projection.IdProjection;

public class Utils {// metodo generico que recebe as outras projection e seus tipos, e usa coringa para herdar da interface

	public static <ID> List<? extends IdProjection <ID>> replace(List<? extends IdProjection <ID>> ordered,
			List<? extends IdProjection <ID>> unordered) {
		
		Map<ID, IdProjection <ID>> map = new HashMap<>();// Map (interface), HashMap(classe concreta). acessa os elementos rapidamente
		
		for (IdProjection <ID> obj : unordered) {//preenche este map com os elementos da lista desordenada
			map.put(obj.getId(), obj);
		}
		
		List<IdProjection <ID>> result = new ArrayList<>();// lista vazia
		
		for (IdProjection <ID> obj : ordered) {// percorre a lista ordenada, acessa o objeto a adiciona na lista vazia
			result.add(map.get(obj.getId()));
		}
		
		return result;
	}

	
}
