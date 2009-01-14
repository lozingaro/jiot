/***************************************************************************
 *   Copyright (C) 2009 by Fabrizio Montesi                                *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/

package jolie.runtime.typing;

import java.util.Map;
import java.util.Map.Entry;
import jolie.lang.NativeType;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.util.Range;

/**
 *
 * @author Fabrizio Montesi
 */
public class Type
{
	final private Range cardinality;
	final private NativeType nativeType;
	final private boolean undefinedSubTypes;
	final private Map< String, Type > subTypes; // TODO this does not need to be a map

	public Type(
		NativeType nativeType,
		Range cardinality,
		boolean undefinedSubTypes,
		Map< String, Type > subTypes
	)
	{
		this.nativeType = nativeType;
		this.cardinality = cardinality;
		this.undefinedSubTypes = undefinedSubTypes;
		this.subTypes = subTypes;
	}

	public void check( Value value )
		throws TypeCheckingException
	{
		if ( checkNativeType( value, nativeType ) == false ) {
			throw new TypeCheckingException( "Invalid native type" );
		}

		if ( undefinedSubTypes == false ) {
			if ( subTypes.size() == value.children().size() ) {
				for( Entry< String, Type > entry : subTypes.entrySet() ) {
					checkSubType( entry.getKey(), entry.getValue(), value );
				}
			} else {
				throw new TypeCheckingException( "Invalid number of child nodes" );
			}
		}
	}

	private void checkSubType( String typeName, Type type, Value value )
		throws TypeCheckingException
	{
		if ( value.hasChildren( typeName ) == false && type.cardinality.min() > 0 ) {
			throw new TypeCheckingException( "Undefined required child node: " + typeName );
		}

		ValueVector vector = value.getChildren( typeName );
		int size = vector.size();
		if ( type.cardinality.min() > size || type.cardinality.max() < size ) {
			throw new TypeCheckingException(
				"Child node " + typeName + " has a wrong number of occurencies. Permitted range is [" +
				type.cardinality.min() + "," + type.cardinality.max() + "], found " + size
			);
		}

		for( Value v : vector ) {
			type.check( v );
		}
	}

	private boolean checkNativeType( Value value, NativeType nativeType )
	{
		if ( nativeType == NativeType.ANY ) {
			return true;
		} else if ( nativeType == NativeType.DOUBLE ) {
			return value.isDouble();
		} else if ( nativeType == NativeType.INT ) {
			return value.isInt();
		} else if ( nativeType == NativeType.STRING ) {
			return value.isString();
		} else if ( nativeType == NativeType.VOID ) {
			return value.valueObject() == null;
		} else if ( nativeType == NativeType.UNDEFINED ) {
			return true;
		}

		return false;
	}
}
