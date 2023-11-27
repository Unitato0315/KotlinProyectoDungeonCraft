package Adaptadores

import Modelo.Carta
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dungeoncrafter.Coleccion
import com.example.dungeoncrafter.R
import com.example.dungeoncrafter.vistaCarta

class AdaptadorCartas (var cartas : ArrayList<Carta>, var  context: Context) : RecyclerView.Adapter<AdaptadorCartas.ViewHolder>() {

    companion object {
        var seleccionado: Int = -1
    }
    /**
     * Se encarga de darno la posicion del elemento seleccionado
     * */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartas[position]
        holder.bind(item, context, position, this)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.cardview_cartas, parent, false)
        val viewHolder = ViewHolder(vista)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, Coleccion::class.java)
            context.startActivity(intent)
        }

        return viewHolder
    }
    override fun getItemCount(): Int {
        return cartas.size
    }
    /**
     * Clase encargada de la vista de cada uno de los elementos del recyclerView
     * Tambien asignamos lo que realiza al pulsar sobre una de las cartas
     * */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Elementos que componen cada carta
        val nombrePersonaje = view.findViewById(R.id.tvNombre) as TextView
        val descripcion = view.findViewById(R.id.edtmlDescripcion) as EditText
        val imPersonaje = view.findViewById(R.id.imgPersonaje) as ImageView
        val imRelic = view.findViewById(R.id.imgRelic) as ImageView
        val imTipe = view.findViewById(R.id.imgTipo) as ImageView
        val cartaCompleta = view.findViewById(R.id.cardView) as CardView

        @SuppressLint("ResourceAsColor")
        fun bind(
            carta: Carta,
            context: Context,
            pos: Int,
            miAdaptadorRecycler: AdaptadorCartas
        ) {
            nombrePersonaje.text = carta.nombre

            val descRecur = context.resources.getStringArray(context.resources.getIdentifier("descripcion","array",context.packageName))
            descripcion.setText(descRecur[carta.descripcion])
            val uri1 = "@drawable/"+carta.imagenPersonaje
            val uri2 = "@drawable/"+carta.imagenRelic
            val uri3 = "@drawable/"+carta.imagenTipo
            val imageResource: Int =
                context.resources.getIdentifier(uri1, null, context.packageName)
            var res: Drawable = context.resources.getDrawable(imageResource)
            imPersonaje.setImageDrawable(res)
            val imageResource2: Int =
                context.resources.getIdentifier(uri2, null, context.packageName)
            var res2: Drawable = context.resources.getDrawable(imageResource2)
            imRelic.setImageDrawable(res2)
            val imageResource3: Int =
                context.resources.getIdentifier(uri3, null, context.packageName)
            var res3: Drawable = context.resources.getDrawable(imageResource3)
            imTipe.setImageDrawable(res3)




            cartaCompleta.setOnClickListener{
               var inte: Intent = Intent(Coleccion.contextoPrincipal, vistaCarta::class.java)
               inte.putExtra("obj", carta)
               ContextCompat.startActivity(Coleccion.contextoPrincipal, inte, null)
            }

            descripcion.setOnClickListener {
                var inte: Intent = Intent(Coleccion.contextoPrincipal, vistaCarta::class.java)
                inte.putExtra("obj", carta)
                ContextCompat.startActivity(Coleccion.contextoPrincipal, inte, null)
            }
        }
    }
}