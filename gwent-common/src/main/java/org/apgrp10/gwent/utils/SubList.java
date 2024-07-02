/********************************
 *          SubList.java
 *      Question 3 (Pokemon)
 *       AP Exercise 1 (OOP)
 *    AmirHossein MohammadZadeh
 *           402106434
 ********************************/
package org.apgrp10.gwent.utils;

import java.util.*;

public class SubList<E> implements List<E> {
	private final ArrayList<E> source;
	private final ArrayList<E> sublist;
	
	public SubList(ArrayList<E> source) {
		this.source = source;
		this.sublist = new ArrayList<>();
	}
	
	@Override
	public int size() {
		checkSource();
		return sublist.size();
	}
	
	@Override
	public boolean isEmpty() {
		checkSource();
		return sublist.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		checkSource();
		return sublist.contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		checkSource();
		return sublist.iterator();
	}
	
	@Override
	public Object[] toArray() {
		checkSource();
		return sublist.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		checkSource();
		return sublist.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		checkSource();
		if (!source.contains(e)) {
			throw new IllegalArgumentException("Element not from source list");
		}
		return sublist.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		checkSource();
		if (!source.contains(o)) {
			throw new IllegalArgumentException("Element not from source list");
		}
		return sublist.remove(o) && source.remove(o);
	}
	
	public boolean unequip(Object o) {
		checkSource();
		if (!source.contains(o)) {
			throw new IllegalArgumentException("Element not from source list");
		}
		return sublist.remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		checkSource();
		return new HashSet<>(sublist).containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		checkSource();
		for (E element : c) {
			if (!source.contains(element)) {
				throw new IllegalArgumentException("Element not from source list");
			}
		}
		return sublist.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		checkSource();
		return sublist.addAll(index, c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		checkSource();
		boolean modified = false;
		for (Object o : c) {
			if (remove(o)) {
				modified = true;
			}
		}
		return modified;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		checkSource();
		return sublist.retainAll(c) && source.retainAll(c);
	}
	
	@Override
	public void clear() {
		checkSource();
		sublist.clear();
		source.retainAll(Collections.emptyList());
	}
	
	@Override
	public E get(int index) {
		checkSource();
		return sublist.get(index);
	}
	
	@Override
	public E set(int index, E element) {
		checkSource();
		if (!source.contains(element)) {
			throw new IllegalArgumentException("Element not from source list");
		}
		E replaced = sublist.set(index, element);
		source.remove(replaced);
		source.add(element);
		return replaced;
	}
	
	@Override
	public void add(int index, E element) {
		checkSource();
		add(element);
	}
	
	@Override
	public E remove(int index) {
		checkSource();
		E removed = sublist.remove(index);
		source.remove(removed);
		return removed;
	}
	
	@Override
	public int indexOf(Object o) {
		checkSource();
		return sublist.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		checkSource();
		return sublist.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<E> listIterator() {
		checkSource();
		return sublist.listIterator();
	}
	
	@Override
	public ListIterator<E> listIterator(int index) {
		checkSource();
		return sublist.listIterator(index);
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		checkSource();
		return sublist.subList(fromIndex, toIndex);
	}
	
	public void checkSource() {
		for (E e : new ArrayList<>(sublist))
			if (!source.contains(e)) sublist.remove(e);
	}
}