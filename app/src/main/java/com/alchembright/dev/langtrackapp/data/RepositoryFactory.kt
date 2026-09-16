package com.alchembright.dev.langtrackapp.data

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.content.Context

class RepositoryFactory {
    companion object {

        private var sRepositoryQr: Repository? = null

        @JvmStatic
        @Synchronized
        fun getRepository(context: Context): Repository {
            if (sRepositoryQr == null) {
                sRepositoryQr =
                    Repository(context)
            }
            return sRepositoryQr as Repository
        }

    }
}