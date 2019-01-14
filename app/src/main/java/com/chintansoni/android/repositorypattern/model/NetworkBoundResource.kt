package com.chintansoni.android.repositorypattern.model

import com.chintansoni.android.repositorypattern.model.Resource.Companion.loading
import com.chintansoni.android.repositorypattern.model.Resource.Companion.success
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

abstract class NetworkBoundResource<LocalType, RemoteType> : FlowableOnSubscribe<Resource<LocalType>> {

    private var emitter: FlowableEmitter<Resource<LocalType>>? = null

    override fun subscribe(emitter: FlowableEmitter<Resource<LocalType>>) {
        this.emitter = emitter
    }

    abstract fun getRemote(): Single<RemoteType>

    abstract fun getLocal(): Single<LocalType>

    abstract fun saveCallResult(data: LocalType, isForced: Boolean)

    abstract fun mapper(): Function<RemoteType, LocalType>

    fun refresh() {
        getRemoteData(true)
    }

    fun fetch(isForced: Boolean) {
        getLocal()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap {
                emitter?.onNext(success(it))
                return@flatMap getRemote()
            }.map(mapper())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap {
                saveCallResult(it, isForced)
                return@flatMap getLocal()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                emitter?.onNext(success(it))
            }, {
                emitter?.onNext(Resource.error(it))
            })
    }

    fun getRemoteData(isForced: Boolean) {
        emitter?.onNext(loading())
        getRemote().map(mapper())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap {
                saveCallResult(it, isForced)
                return@flatMap getLocal()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                emitter?.onNext(success(it))
            }, {
                emitter?.onNext(Resource.error(it))
            })
    }
}