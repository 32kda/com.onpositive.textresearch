package com.onpositive.semantic.words2.builder;

import java.io.Serializable;


public class NounFormRule implements Serializable,IFormRule{
	
	public static final int NOM_SG=NounRelations.NOM_SG;
	public static final int NOM_PL=NounRelations.NOM_PL;
	
	public static final int GEN_SG=NounRelations.GEN_SG;
	public static final int GEN_PL=NounRelations.GEN_PL;
	
	public static final int DAT_SG=NounRelations.DAT_SG;
	public static final int DAT_PL=NounRelations.DAT_PL;
	
	public static final int ACC_SG=NounRelations.ACC_SG;
	public static final int ACC_PL=NounRelations.ACC_PL;
	
	public static final int INS_SG=NounRelations.INS_SG;
	public static final int INS_PL=NounRelations.INS_PL;
	
	public static final int PRP_SG=NounRelations.PRP_SG;
	public static final int PRP_PL=NounRelations.PRP_PL;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String st;
	String pt;
	/**
	 * Именительный единственное 
	 */
	String nom_sg;
	/**
	 * Именительный множественное
	 */
	String nom_pl;
	
	/**
	 * Родительный единственное 
	 */
	String gen_sg;
	/**
	 * Родительный множественное
	 */
	String gen_pl;
	
	/**
	 * Дательный единственное 
	 */
	String dat_sg;
	/**
	 * Дательный множественное
	 */
	String dat_pl;
	
	/**
	 * Дательный единственное 
	 */
	String acc_sg;
	/**
	 * Дательный множественное
	 */
	String acc_pl;
	
	/**
	 * Творительный единственное
	 */
	String ins_sg;
	
	/**
	 * Творительный множенственное
	 */
	String ins_pl;
	
	/**
	 * Предложный единственное
	 */
	String prp_sg;
	
	/**
	 * Предложный множественное
	 */
	String prp_pl;
}