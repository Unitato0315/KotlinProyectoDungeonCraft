package Adaptadores

import Modelo.Almacen
import Modelo.Carta
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.dungeoncrafter.Coleccion
import com.example.dungeoncrafter.R

class AdaptadorCartas (var cartas : ArrayList<Carta>, var  context: Context) : RecyclerView.Adapter<AdaptadorCartas.ViewHolder>() {

    companion object {
        var seleccionado: Int = -1
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartas[position]
        holder.bind(item, context, position, this)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.cardview_cartas, parent, false)
        val viewHolder = ViewHolder(vista)
        // Configurar el OnClickListener para pasar a la segunda ventana.
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, Coleccion::class.java)
            context.startActivity(intent)
        }

        return viewHolder
    }
    override fun getItemCount(): Int {
        return cartas.size
    }


    //--------------------------------- Clase interna ViewHolder -----------------------------------
    /**
     * La clase ViewHolder. No es necesaria hacerla dentro del adapter, pero como van tan ligadas
     * se puede declarar aquí.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //Esto solo se asocia la primera vez que se llama a la clase, en el método onCreate de la clase que contiene a esta.
        //Por eso no hace falta que hagamos lo que hacíamos en el método getView de los adaptadores para las listsViews.
        //val nombrePersonaje = view.findViewById(R.id.txtNombre) as TextView
        //val tipoPersonaje = view.findViewById(R.id.txtTipo) as TextView
        //val avatar = view.findViewById(R.id.imgImagen) as ImageView

        //Como en el ejemplo general de las listas (ProbandoListas) vemos que se puede inflar cada elemento con una card o con un layout.
        val nombrePersonaje = view.findViewById(R.id.tvNombre) as TextView
        val descripcion = view.findViewById(R.id.edtmlDescripcion) as EditText
        val imPersonaje = view.findViewById(R.id.imgPersonaje) as ImageView
        val imRelic = view.findViewById(R.id.imgRelic) as ImageView
        val imTipe = view.findViewById(R.id.imgTipo) as ImageView

        val btnDetalleEspcifico = view.findViewById<Button>(R.id.btnDetalle) as Button

        @SuppressLint("ResourceAsColor")
        fun bind(
            carta: Carta,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: AdaptadorCartas
        ) {
            nombrePersonaje.text = carta.nombre
            descripcion.setText(carta.descripcion)
            val uri1 = "@drawable/"+carta.imagenPersonaje
            val uri2 = "@drawable/"+carta.imagenRelic
            val uri3 = "@drawable/"+carta.imagenTipo
            val imageResource: Int =
                context.getResources().getIdentifier(uri1, null, context.packageName)
            var res: Drawable = context.resources.getDrawable(imageResource)
            imPersonaje.setImageDrawable(res)
            val imageResource2: Int =
                context.getResources().getIdentifier(uri2, null, context.packageName)
            var res2: Drawable = context.resources.getDrawable(imageResource2)
            imRelic.setImageDrawable(res2)
            val imageResource3: Int =
                context.getResources().getIdentifier(uri3, null, context.packageName)
            var res3: Drawable = context.resources.getDrawable(imageResource3)
            imTipe.setImageDrawable(res3)

            //Para marcar o desmarcar al seleccionado usamos el siguiente código.
            //comparo la posición y pinto en el color elegido(blue)
            //está implementado de dos maneras, uan deprecated y actual.
            if (pos == AdaptadorCartas.seleccionado) {
                with(nombrePersonaje) {
                    this.setTextColor(resources.getColor(R.color.md_theme_dark_onTertiary))
                }
                descripcion.setTextColor(R.color.md_theme_dark_onTertiary)
            } else {
                with(nombrePersonaje) {
                    this.setTextColor(resources.getColor(R.color.md_theme_light_shadow))
                }
                descripcion.setTextColor(R.color.md_theme_light_shadow)
            }

//            itemView.setOnLongClickListener(View.OnLongClickListener() {
//                Log.e("ACSC0","long click")
//            }

            //Se levanta una escucha para cada item. Si pulsamos el seleccionado pondremos la selección a -1, (deselecciona)
            // en otro caso será el nuevo sleccionado.
            itemView.setOnClickListener {
                if (pos == AdaptadorCartas.seleccionado) {
                    AdaptadorCartas.seleccionado = -1
                } else {
                    AdaptadorCartas.seleccionado = pos
                    Log.e(
                        "ACSC0",
                        "Seleccionado: ${
                            Almacen.Cartas.get(AdaptadorCartas.seleccionado).toString()
                        }"
                    )
                }
                //Con la siguiente instrucción forzamos a recargar el viewHolder porque han cambiado los datos. Así pintará al seleccionado.

                miAdaptadorRecycler.notifyDataSetChanged()

//                val intent = Intent(context, MainActivity2::class.java)
//
//                context.startActivity(intent)

                Toast.makeText(
                    context,
                    "Valor seleccionado " + AdaptadorCartas.seleccionado.toString(),
                    Toast.LENGTH_SHORT
                ).show()

            }
            itemView.setOnLongClickListener(View.OnLongClickListener {
                Log.e(
                    "ACSCO",
                    "Seleccionado con long click: ${Almacen.Cartas.get(pos).toString()}"
                )
                Almacen.Cartas.removeAt(pos)
                miAdaptadorRecycler.notifyDataSetChanged()
                true //Tenemos que devolver un valor boolean.
            })


            btnDetalleEspcifico.setOnClickListener {
               // var inte: Intent = Intent(MainActivity.contextoPrincipal, MainActivity2::class.java)
               // inte.putExtra("obj", pers)
               //ContextCompat.startActivity(MainActivity.contextoPrincipal, inte, null)
            }
        }
    }
}