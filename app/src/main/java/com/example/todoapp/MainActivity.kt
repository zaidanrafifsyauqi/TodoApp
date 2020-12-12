package com.example.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Buat variabel Database Reference yang akan diisi oleh database firebase
    private lateinit var databaseRef: DatabaseReference

    //variabel cekData di buat global
    private lateinit var cekData : DatabaseReference

    //buat variabel datalistener global bernama readDataListener
    private lateinit var readDataListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseRef = FirebaseDatabase.getInstance().reference

        // ketika tombol tambah diklik
        btn_tambah.setOnClickListener {
            // ambil teks dari edittext input_nama
            val nama = input_nama.text.toString()
            // cek apakah nama kosong, jika kosong maka hentikan fungsi hapusData
            if (nama.isBlank()) {
                toastData("Kolom Nama harus diisi")
            } else {
                // jalankan fungsi tambahData
                tambahData(nama)
            }
        }

        // ketika tombol hapus diklik
        btn_hapus.setOnClickListener {
            // ambil teks dari edittext input_nama
            val nama = input_nama.text.toString()
            // cek apakah nama kosong, jika kosong maka hentikan fungsi hapusData
            if (nama.isBlank()) {
                toastData("Kolom Nama harus diisi")
            } else {
                // jalankan fungsi hapusData
                hapusData(nama)
            }
        }

        btn_edit.setOnClickListener {
            val namaAsal = input_nama.text.toString()
            val namaTujuan = edit_nama.text.toString()
            if (namaAsal.isBlank() || namaTujuan.isBlank()) {
                toastData("Kolom tidak boleh kosong")
            } else {
                modifData(namaAsal, namaTujuan)
            }
        }

        // jalankan fungsi cekData di onCreate
        cekData()
    }

    private fun cekData() {
        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // cek apakah ada data di dalam folder tujuan
                if(snapshot.childrenCount > 0) {
                    var textData = ""
                    for(data in snapshot.children) {
                        val nilai = data.getValue(ModelNama::class.java) as ModelNama
                        textData += "${nilai.Nama} \n"
                    }
                    txt_nama.text = textData
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }

        //cekData menuju ke database firebase bagian "Daftar Nama"
        cekData = databaseRef.child("Daftar Nama")
        // addValueEventListener digunakan untuk membantu perubahan database di folder Daftar nama
        cekData.addValueEventListener(dataListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        //hapus pemantau (event listener) pada cekData yang berisi folder "Daftar Nama"
        cekData.removeEventListener(readDataListener)
    }

    private fun modifData(namaAsal: String, namaTujuan: String) {
        // logika modifikasi data diletakkan di bagian ValueEventListener
        // logikanya cek data namaAsal dulu, jika ada  maka modif data tsb dengan namaTujuan
        val dataTujuan = HashMap<String, Any>()
        dataTujuan["Nama"] = namaTujuan

        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0) {
                    databaseRef.child("Daftar Nama")
                        .child(namaAsal)
                        .updateChildren(dataTujuan)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) toastData("Data Telah diupdate")
                        }
                } else {
                    toastData("Data yang dituju tidak ada di database")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        val dataAsal = databaseRef.child("Daftar Nama")
            .child(namaAsal)
        dataAsal.addListenerForSingleValueEvent(dataListener)
    }

    private fun hapusData(nama: String) {
        // membuat listener data firebase
        val dataListener = object : ValueEventListener {
            // onDataChange itu untuk mengetahui aktifitas data
            // seperti penambahan, pengurangan, dan perubahan data
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.childrenCount untuk mengetahui banyak data yang telah diambil
                if (snapshot.childrenCount > 0) {
                    // jika data tersebut ada, maka hapus data saja
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("$nama telah dihapus")
                        }
                } else {
                    toastData("Tidak ada data $nama")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastData("tidak bisa menghapus data itu")
            }
        }
        // untuk menghapus data, kita perlu cek data terlebih dahulu
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
        // addValueEventListener itu menjalankan Listener terus menerus selama data yang diinputkan sama
        // sedangkan addListenerForSingleValueEvent itu dijalankan sekali saja
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    private fun toastData(pesan: String) {
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String, Any>()
        data["Nama"] = nama

        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0) {
                    toastData("Data Tersebut telah ada di database")
                } else {
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toastData("$nama telah ditambahkan dalam database")
                        } else {
                            toastData("$nama gagal ditambahkan")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                toastData("tidak bisa menghapus data itu")
            }
        }
        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(dataListener)
    }
}