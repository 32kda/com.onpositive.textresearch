package com.onpositive.semantic.words2.builder;

import java.io.Serializable;


public class VerbFormRule implements IFormRule,Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int IA=VerbRelations.IA;
	public static final int IA_PAST=VerbRelations.IA_PAST;
	public static final int WE=VerbRelations.WE;
	public static final int WE_PAST=VerbRelations.WE_PAST;
	
	public static final int YOU=VerbRelations.YOU;
	public static final int YOU_PAST=VerbRelations.YOU_PAST;
	
	public static final int THOU=VerbRelations.THOU;
	public static final int THOU_PAST=VerbRelations.THOU_PAST;
	public static final int YOU_DIRECT=VerbRelations.YOU_DIRECT;
	
	
	public static final int ON_ONA_ON=VerbRelations.ON_ONA_ON;
	public static final int ON_ONA_ONO_PAST=VerbRelations.ON_ONA_ONO_PAST;
	
	public static final int ONI=VerbRelations.ONI;
	public static final int ONI_PAST=VerbRelations.ONI_PAST;
	
	public static final int FUTURE=VerbRelations.FUTURE;
	public static final int INFINITIVE=VerbRelations.INFINITIVE;
	
	@FieldMapping(value="Я",relation=IA)
	String ia;
	
	@FieldMapping(value="Я (прош.)",relation=IA_PAST)
	String ia_past;
	
	@FieldMapping(value="Мы",relation=WE)
	String we;
	
	@FieldMapping(value="Мы (прош.)",relation=WE_PAST)
	String we_past;
	
	@FieldMapping(value="Ты",relation=THOU)
	String thou;
	
	@FieldMapping(value="Ты (прош.)",relation=THOU_PAST)
	String thou_past;
	
	@FieldMapping(value="Ты (повел.)",relation=YOU_DIRECT)
	String thou_direct;
	
	@FieldMapping(value="Вы",relation=YOU)
	String you;
	
	@FieldMapping(value="Вы (прош.)",relation=YOU_PAST)
	String you_past;
	
	@FieldMapping(value="Вы (повел.)",relation=YOU_DIRECT)
	String you_direct;
	
	@FieldMapping(value="Он/она/оно",relation=ON_ONA_ON)
	String third_form;
	
	@FieldMapping(value="Он/она/оно (прош.)",relation=ON_ONA_ONO_PAST)
	String third_form_past;
	
	@FieldMapping(value="Они",relation=YOU_DIRECT)
	String they;
	
	@FieldMapping(value="Они (прош.)",relation=YOU_DIRECT)
	String they_past;
	
	@FieldMapping(value="Будущее",relation=FUTURE)
	String future;
	
	@FieldMapping(value="Инфинитив",relation=INFINITIVE)
	String infinitive;


/*	|Я          ={{{основа1}}}ю
			|Я (прош.) ={{{основа}}}л<br />{{{основа}}}ла
			|Мы ={{{основа1}}}ем
			|Мы (прош.) ={{{основа}}}ли
			|Ты ={{{основа1}}}ешь
			|Ты (прош.) ={{{основа}}}л<br />{{{основа}}}ла
			|Ты (повел.)={{{основа1}}}й
			|Вы ={{{основа1}}}ете
			|Вы (прош.) ={{{основа}}}ли
			|Вы (повел.)={{{основа1}}}йте
			|Он/она/оно ={{{основа1}}}ет
			|Он/она/оно (прош.)={{{основа}}}л<br />{{{основа}}}ла<br />{{{основа}}}ло
			|Они ={{{основа1}}}ют
			|Они (прош.)={{{основа}}}ли
			|ПричНаст = {{{основа1}}}ющий
			|ПричПрош = {{{основа}}}вший
			|ДеепрНаст = {{{основа1}}}я
			|ДеепрПрош = {{{основа}}}в, {{{основа}}}вши
			|ПричСтрад = {{{основа1}}}емый
			|ПричСтрадПрош = {{#if:{{{2в|}}}|{{{основа2}}}анный|{{{ПричСтрадПрош|}}}}}
			|Будущее = буду/будешь… {{{основа}}}ть
			|Инфинитив = {{{основа}}}ть
			|hide-text={{{hide-text|}}}
			|слоги={{{слоги|}}}
			|спряжение=2a
			|вид={{#if:{{{2в|}}}|2|н}}
			|НП={{{НП|}}}
			|соотв={{{соотв|}}}
			|соотв-мн={{{соотв-мн|}}}
			|коммент={{{коммент|}}}
			}}*/
	public VerbFormRule(){
		
	}
}
