package com.chintansoni.android.repositorypattern.di.module

import com.chintansoni.android.repositorypattern.UsersActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeUsersActivity(): UsersActivity
}